(ns timeless.util
  "The namespace for the utility functions and tools used in several namespaces."
  (:require [cheshire.core :as json]
											 [clj-time.core :refer [time-zone-for-id
                                   now plus minus weeks hours date-time]]
            [clj-time.format :refer [formatter parse]]
            [clojure.core.memoize :as memo]
            [clojure.java.classpath :as cp]
            [clojure.java.shell :as shell]
            [clojure.string :as cs]
            [clojure.tools.logging :refer [infof warn warnf error errorf]]
            [clojure.walk :refer [prewalk]])
  (:import java.util.UUID
           java.util.zip.ZipInputStream
           org.joda.time.DateTime))

;; simple definitions to make the code cleaner
(def min-in-millis (* 60 1000))

;;
;; Let's expand cheshire's understanding of JSON encoding by telling it how to
;; deal with a Joda Time. This will make all the encoding a lot simpler because
;; we don't have to worry about changing the Joda time values into Jada Dates.
;;
(extend-protocol cheshire.generate/JSONable
  org.joda.time.DateTime
  (to-json [t jg]
    (cheshire.generate/write-string jg (str t))))

(declare remove-nil-keys)

(def ascii-0 (int \0))
(def ascii-9 (int \9))
(def ascii-punct #{ \( \) \. \; })

(defn is-digit?
  "Predicate function to return 'true' if the supplied character is an ASCII
  digit: 0-9. This is helpful in scanning string data using filters."
  [c]
  (<= ascii-0 (int c) ascii-9))

(defn is-int-char?
  "Predicate function to return 'true' if the supplied character is a valid
  ASCII character for an int: 0-9 or '-'. This is helpful in scanning string
  data using filters."
  [c]
  (or (is-digit? c) (= \- c)))

(defn is-double-char?
  "Predicate function to return 'true' if the supplied character is a valid
  ASCII character for a double: 0-9, '.', '-'. This is helpful in scanning
  string data using filters."
  [c]
  (or (is-int-char? c) (= \. c)))

(defn joda?
  "Simple predicate function to indicate if the argument is a Joda Date/time
  which is very handy to have in other functions, and makes sense with the other
  predicate type funcitons we have from Clojure."
  [arg]
  (if arg
    (instance? org.joda.time.DateTime arg)
    false))

(defn uuid
  "Function to create a new UUID for any reason whatsoever. In this namespace,
  it's going to be very handy to have as we need to make sure that we have some
  way of uniquely identifying the different consumers of messages."
  []
  (UUID/randomUUID))

(defn uuid-str
  "Function to create a new UUID, as a String, for any reason whatsoever. In
  this namespace, it's going to be very handy to have as we need to make sure
  that we have some way of uniquely identifying the different consumers of
  messages. And kafka needs to have strings for identifiers as opposed to
  general Objects."
  []
  (str (uuid)))

(def uuid-char-set
  (set "0123456789abcdef-"))

(defn to-uuid
  "Function to convert a string (as if from JSON) and convert it to a UUID in the
  clojure datatypes. This is a simple, but necessary conversion for the JSON data
  from Hadoop."
  [s]
  (cond
    (nil? s) nil
    (instance? java.util.UUID s) s
    (string? s) (let [s' (cs/trim s)]
                  (if (and (= 36 (count s'))
                        (every? uuid-char-set (cs/lower-case s'))
                        (= (nth s' 8) (nth s' 13) (nth s' 18) (nth s' 23)))
                    (try
                      (UUID/fromString s')
                      (catch java.lang.IllegalArgumentException iae
                        nil))))
    :else nil))

(defn ucase
  "Function to convert the string argument to it's upper-case equivalent - but
  do so in a clean manner. This means that if it's a `nil`, you get back a `nil`,
  and if it's a number, you get that number back. This was left out of the spec
  in the original, and it makes for code like this to clean things up. If you
  pass in a collection, this function will call itself on all the values in that
  collection so you can upper-case a collection without worrying about the other
  types of values."
  [s]
  (cond
    (string? s) (cs/upper-case s)
    (coll? s)   (map ucase s)
    :else       s))

(defn lcase
  "Function to convert the string argument to it's lower-case equivalent - but
  do so in a clean manner. This means that if it's a `nil`, you get back a `nil`,
  and if it's a number, you get that number back. This was left out of the spec
  in the original, and it makes for code like this to clean things up. If you
  pass in a collection, this function will call itself on all the values in that
  collection so you can lower-case a collection without worrying about the other
  types of values."
  [s]
  (cond
    (string? s) (cs/lower-case s)
    (coll? s)   (map lcase s)
    :else       s))

(defn trim
  "Function to trim the white space off the the string argument - but
  do so in a clean manner. This means that if it's a `nil`, you get back a `nil`,
  and if it's a number, you get that number back. This was left out of the spec
  in the original, and it makes for code like this to clean things up. If you
  pass in a collection, this function will call itself on all the values in that
  collection so you can lower-case a collection without worrying about the other
  types of values."
  [s]
  (cond
    (string? s) (cs/trim s)
    (coll? s)   (map trim s)
    :else       s))

(defn nil-if-empty
  "Function to ensure that if we have an empty string, map or collection,
  it's converted to a nil so that there are no empty strings, maps or
  collections in the data sets. These really shouldn't be there, but they
  sneak in from time to time."
  [arg]
  (cond
    (nil? arg) nil
    (or (string? arg) (map? arg) (coll? arg)) (if-not (empty? arg) arg)
    :else arg))

(defn nil-if-zero
  "Function to ensure that if we have a zero, it's converted to a nil so that
  there are no zeros in the data sets. These are most likely invalid parsing
  valus from the data, as we need to nil them out as opposed to passing on
  bad data."
  [arg]
  (cond
    (nil? arg) nil
    (and (number? arg) (zero? arg)) nil
    :else arg))

(defn update-keys
  "Like update (above), but operates on multiple keys at once. Note
  this is different from update-in (though they have the same argument
  signature), which uses the list of keys as a path into the nested
  data structure.  For update-keys, all the keys are top-level."
  [m ks f & args]
  (reduce #(apply update %1 %2 f args)
          m
          ks))

(defn update-existing-keys
  "Similar to update-keys (above), but operates only on existing keys
  in the map. Note this is different from update-in (though they have
  the same argument signature), which uses the list of keys as a path
  into the a nested data structure. For update-existing-keys, all the
  keys are top-level, and ignored if they aren't present."
  [m ks f & args]
  (reduce #(if (contains? %1 %2) (apply update %1 %2 f args) %1)
          m
          ks))

(defn update-all
  "Function to apply the provided function to *all* values in the provided
  map. The result is a map with all the values changed by the function."
  [m f & args]
  (reduce-kv (fn [m' k v] (assoc m' k (apply f v args))) {} m))

(defn rename-keys
  "This is completely compatible with `clojure.set/rename-keys`.
  But in addition to that behavior,
    - If the second arg is a map and the value of a mapping is a function then the
    function will be called with the key and the result will be used as the new key.
    - If the second arg is a function then all the keys of `map` will be transformed
    by it.
    - If the second arg is sequential then only the keys in the sequence will be renamed
    according to the third argument, assumed to be a function (identity is used if
    there is not third argument)."
  [map & [km_f_s f]]
  (let [kmap (cond
               (map? km_f_s) km_f_s
               (fn? km_f_s) (zipmap (keys map) (repeat km_f_s))
               (sequential? km_f_s) (zipmap km_f_s (repeat (or f identity)))
               :else {km_f_s (or f identity)})]
    (reduce
      (fn [m [old new]]
        (let [fnew (if (fn? new) new (fn [_] new))]
          (if (contains? map old)
            (assoc m (fnew old) (get map old))
            m)))
      (apply dissoc map (keys kmap)) kmap)))

(defn rename-keys-deep
  "Function that recursively applies of `f` to any values in `val` that are maps
  or a collection of maps."
  [f val]
  (prewalk #(if (map? %) (f %) %) val))

(defn compact
  "Simple convenience function to remove the nils from a collection, or
  nil- or empty-valued keys from a map, but leave everything else alone.
  This is just like the ruby compact method, and it's really quite useful for
  sums and operations that will choke on nils."
  [s]
  (cond
    (map? s)  (-> (update-all s nil-if-empty)
                  (remove-nil-keys))
    (set? s)  (disj s nil)
    (coll? s) (remove nil? s)
    :else     s))

(defn compact-to-nil
  "Function to remove all nils in a sequence and returns nil if the sequence
  returns as empty."
  [s]
  (if-let [cs (compact s)]
    (if-not (empty? cs) cs)))

(defn remove-nil-keys
  "Given a map, returns a new map with all keys whose values are nil removed."
  [m]
  (reduce (fn [m [k v]] (if (nil? v) (dissoc m k) m)) m m))

(defn remove-nil-keys-deep
  "Convenience function that combines `rename-keys-deep` and `remove-nil-keys`
  to remove all keys whose values are `nil` at the root of the map and inside
  any nested maps or collections of maps."
  [m]
  (rename-keys-deep remove-nil-keys m))

;; ## Git Introspection Tools

(defn container*
  "Tries to return the _running container jar_ of this process, if one exists.
  If not, a `nil` is returned. If we're in the `clojure-x.y.x.jar`, then it's
  Leiningen, and return `nil`."
  []
  (if-let [jar (-> clojure.lang.RT
                  .getProtectionDomain
                  .getCodeSource
                  .getLocation
                  .getPath
                  java.io.File.
                  .getName)]
    (if-not (.startsWith jar "clojure-")
      jar)))

(def container
  "Memoized function that tries to return the _running container jar_ of
  this process, if one exists. If not, a `nil` is returned. If we're in
  the `clojure-x.y.x.jar`, then it's Leiningen, and return `nil`."
  (memo/lru container* :lru/threshold 2))

(defn git-commit*
  "Tries to determine the currently deployed commit by looking for it
  in the name of the jar. Returns nil if it cannot be determined."
  []
  (-> (or (container) "")
      (->> (re-find #"-([a-f0-9]{5,})\.jar"))
      second))

(def git-commit
  "Memoized function that tries to determine the currently deployed
  commit by looking for it in the name of the jar. Returns nil if it
  cannot be determined."
  (memo/lru git-commit* :lru/threshold 2))

(defn git-head*
  "Function to call out to the system and get from git, the SHA for the HEAD
  that the current repo is on, and extract the first 8 characters of that
  SHA. If there are any issues, a `nil` will be returned."
  []
  (let [here (try (shell/sh "git" "rev-parse" "HEAD") (catch Throwable t))
        sha (nil-if-empty (:out here))]
    (if sha
      (.subSequence sha 0 8))))

(def git-head
  "Memoized function to call out to the system and get from git, the SHA for
  the HEAD that the current repo is on, and extract the first 8 characters of
  that SHA. If there are any issues, a `nil` will be returned."
  (memo/lru git-head* :lru/threshold 2))

(defn git-sha*
  "Function to get the _best_ git SHA for the current codebase. This will first
  see if it's in the `git-commit`, and if not, then it'll look to the current
  `git-head` and tag it as iffy."
  []
  (or (git-commit)
    (if-let [sha (git-head)]
      (str sha "+"))))

(def git-sha
  "Memoized function to get the _best_ git SHA for the current codebase. This
  will first see if it's in the `git-commit`, and if not, then it'll look to
  the current `git-head` and tag it as iffy."
  (memo/lru git-sha* :lru/threshold 2))

(defn project-version*
  "Function to look at the 'project.clj' file and pick out the version of the
  project for use *within* the code itself. This makes it easy to define - or
  report - the version of this project without having to maintain it in
  multiple places."
  []
  (if-let [local (try (-> "project.clj" slurp) (catch Throwable t))]
    (-> local read-string (nth 2))
    (let [jname (or (container) "")
          bname (nil-if-empty (cs/replace jname #"\-(\d+\.\d+\.\d+.*|[a-f0-9]{8})\.jar$" ""))]
      (if bname
        (let [pname (format "META-INF/leiningen/%s/%s/project.clj" bname bname)
              me? (fn [j] (.endsWith (.getName j) jname))
              jar (first (filter me? (cp/classpath-jarfiles)))
              proj (if jar (some->> (.getJarEntry jar pname)
                                    (.getInputStream jar)
                                    (slurp)))]
          (if proj
            (-> proj read-string (nth 2))))))))

(def project-version
  "Memoized function to look at the 'project.clj' file and pick out the
  version of the project for use *within* the code itself. This makes it
  easy to define - or report - the version of this project without having
  to maintain it in multiple places."
  (memo/lru project-version* :lru/threshold 2))

;; ## General Parsers

(defn parse-int
  "Parses a string into an int, expecting \"Inf\" for infinity. A nil is parsed
  as 0 by default - similar to ruby's `to_i` method. The default can be overriden
  by passing a named argument,\n  e.g. `(parse-int val :default true)`."
  [x & {:keys [default] :or {default 0}}]
  (cond
    (nil? x) default
    (or (= "NA" x) (= "Inf" x) (= "Infinity" x)) Integer/MAX_VALUE
    (or (= "-Inf" x) (= "-Infinity" x)) Integer/MIN_VALUE
    (string? x) (cond
                  (empty? x) default
                  (some #(not (is-int-char? %)) x) default
                  :else (try
                          (Integer/parseInt (cs/trim x))
                          (catch java.lang.NumberFormatException nfe
                            (infof "Unable to parse '%s' into an integer!" x)
                            default)))
    (coll? x) (map parse-int x)
    (number? x) (int (if (pos? x) (min x Integer/MAX_VALUE) (max x Integer/MIN_VALUE)))
    :else x))

(defn parse-long
  "Function to parse a long from a string - but it might not be a string and it
  might be null, and it might have whitespace - so I have to trim it and then
  parse it, but a null at any point is going to be disaster. So this function
  makes sure it's properly conditioned before parsing."
  [x & {:keys [default] :or {default 0}}]
  (cond
    (nil? x) default
    (or (= "NA" x) (= "Inf" x) (= "Infinity" x)) Long/MAX_VALUE
    (or (= "-Inf" x) (= "-Infinity" x)) Long/MIN_VALUE
    (string? x) (cond
                  (empty? x) default
                  (some #(not (is-int-char? %)) x) default
                  (< 19 (count x)) default
                  :else (try
                          (Long/parseLong (cs/trim x))
                          (catch java.lang.NumberFormatException nfe
                            (infof "Unable to parse '%s' into a long!" x)
                            default)))
    (coll? x) (map parse-long x)
    (number? x) (long (if (pos? x) (min x Long/MAX_VALUE) (max x Long/MIN_VALUE)))
    :else x))

