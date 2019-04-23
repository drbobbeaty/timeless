(ns timeless.core
  "The functions used to implement the service logic."
  (:require [clj-time.coerce :refer [to-long]]
  										[clj-time.core :refer [now time-zone-for-id]]
  								  [clj-time.format :refer [formatter parse]]
            [clojure.tools.logging :refer [info infof warn warnf error errorf]]
            [timeless.logging :refer [log-execution-time!]]
            [timeless.util :refer [parse-int parse-long joda?]]
            ))

(def timeline-primary
  "This is a custom formatter to allow us to parse Timmeline date time strings."
  (formatter
    (time-zone-for-id "US/Central")
    "yyyy-MM-dd HH:mm:ss.SSS"
    "yyyy/MM/dd HH:mm:ss.SSS"))

(defn parse-dt
  "Function to parse a date and time string into a Joda DateTime.
  Returns the argument if it's not a string or can't be parsed."
  [s]
  (if (and (string? s) (not-empty s))
    (try
      (or (parse-long s :default nil) (parse timeline-primary s))
      (catch java.lang.IllegalArgumentException iae
        (warnf "Unable to parse %s into a DateTime! %s" s (.getMessage iae))
        s))
    s))

(defn epoch-msec
  "Function to take an argument, and try to parse it into a Joda date/time and
  then get the msec since epoch, and return that. If the data is a long, then
  leave it as it is, as it's probably already been converted."
  [s]
  (let [j (parse-dt s)]
  	 (cond
  	   (number? j) j
  	   (joda? j)   (to-long j)
  	   :else       0)))
