(ns timeless.server
  "The routes for the RESTful service that will allow folks to get at the
  exposed functions in a simple, fast, and efficient way."
  (:require [camel-snake-kebab.core :as csk]
            [cheshire.core :as json]
            [clj-time.coerce :refer [to-long from-long]]
            [clj-time.core :refer [now]]
            [clojure.pprint :refer [pprint]]
            [clojure.string :as cs]
            [clojure.tools.logging :refer [infof warn warnf error errorf]]
            [compojure
             [core :refer [defroutes GET POST PUT DELETE]]
             [handler :as handler]
             [response :refer [render]]
             [route :as route]]
            [timeless.core :as core]
            [timeless.util :as util]
            [timeless.logging :refer [log-execution-time!]]
            [timeless.util :refer [ucase git-commit git-sha project-version
                                     nil-if-zero nil-if-empty to-uuid
                                     remove-nil-keys update-keys
                                     ]]
            [ring.middleware.cors :as cors]
            [ring.middleware.cookies :refer [wrap-cookies]]
            [ring.middleware.jsonp :refer [wrap-json-with-padding]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.ssl :refer [wrap-ssl-redirect]]
            [ring.middleware.gzip :refer [wrap-gzip]]
            [ring.util.response :as resp]
            [ring.util.io :refer [piped-input-stream]])
  (:import [java.io BufferedWriter OutputStreamWriter IOException StringWriter]))

(extend-protocol cheshire.generate/JSONable
  org.joda.time.DateTime
  (to-json [t jg]
    (cheshire.generate/write-string jg (str t))))

(def wrap-cors
  "This is a simple convenience definition for enabling the CORS support in
  middleware for a service. The use of this will be far simpler than including
  all this in every service."
  #(cors/wrap-cors % :access-control-allow-origin #".+"
                     :access-control-allow-headers ["Origin" "Content-Type" "Accept"
                                                    "Authorization" "Last-Modified"
                                                    "Credentials" "X-Request-Id"
                                                    "X-Session-Id"]
                     :access-control-allow-methods [:get :put :post :delete]))

(defn not-found
  "compojure.route/not-found always returns a 404 status, which causes issues
  for some of our front-ends. We take in a body just like compojure's not-found,
  but allow for status to be passed in if needed."
  [body & [status]]
  (let [cb (atom {})]
    (fn [request]
      (-> (render
            (cond
              (string? body) body
              (map? body)    (if (empty? @cb)
                               (reset! cb
                                 (if (instance? java.io.InputStream (:body body))
                                   (update body :body slurp)
                                   body))
                               @cb)
              (fn? body)     (body)
              :else          "")
            request)
          (resp/status (or status 404))
          (cond-> (= (:request-method request) :head) (assoc :body nil))))))

(defn return-code
  "Creates a ring response for returning the given return code."
  [code]
  {:status code
   :headers {"Content-Type" "application/json; charset=UTF-8"}})

(defn return-json
  "Creates a ring response for returning the given object as JSON."
  ([ob] (return-json ob (now) (if (nil? ob) 204 200)))
  ([ob lastm] (return-json ob lastm (if (nil? ob) 204 200)))
  ([ob lastm code]
    {:status (if (nil? ob) 204 code)
     :headers {"Content-Type" "application/json; charset=UTF-8"
               "Last-Modified" (str (or lastm (now)))}
     :body (piped-input-stream
             (bound-fn [out]
               (with-open [osw (OutputStreamWriter. out)
                           bw (BufferedWriter. osw)]
                 (let [error-streaming
                       (fn [e]
                         ;; Since the HTTP headers have already been sent,
                         ;; at this point it is too late to report the error
                         ;; as a 500. The best we can do is abruptly print
                         ;; an error and quit.
                         (.write bw "\n\n---ERROR WHILE STREAMING JSON---\n")
                         (.write bw (str e "\n\n"))
                         (warnf "Streaming exception for JSONP: %s" (.getMessage e)))]
                   (try
                     (json/generate-stream ob bw)
                     ;; Handle "pipe closed" errors
                     (catch IOException e
                       (if (re-find #"Pipe closed" (.getMessage e))
                         (infof "Pipe Closed exception: %s" (.getMessage e))
                         (error-streaming e)))
                     (catch Throwable t
                       (error-streaming t)))))))}))

(defn index-page
  "Returns the content of index.html as a response."
  []
  (-> (resp/resource-response "index.html" {:root "public"})
      (resp/header "Content-Type" "text/html; charset=UTF-8")))

(defroutes app-routes
  "Primary routes for the webserver."
  (GET "/" []
    (index-page))
  (GET "/info" []
    (return-json {:app "timeless"
                  :version (project-version)
                  :code (or (git-sha) "unknown commit")}))
  (GET "/heartbeat" []
    (return-code 200))

  (GET "/v1/epochize/:arg" [arg]
    (return-json {:epochMsec (core/epoch-msec arg)}))

  ;; Finish up with the static resources and the 404 page
  (route/resources "/")
  (not-found (index-page) 200))

(def app
  "The actual ring handler that is run -- this is the routes above
   wrapped in various middlewares."
  (-> app-routes
      wrap-json-with-padding
      handler/site
      wrap-cors
      wrap-params
      wrap-cookies
      wrap-gzip))
