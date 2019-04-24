(ns timeless.main
  "This doesn't do a lot, but it's here to launch the different modes of
  the app, and clean up afterwards."
  (:require [clojure.java.io :refer [resource input-stream]]
            [clojure.tools.cli :refer [cli]]
            [clojure.tools.logging :refer [info infof warn warnf error errorf]]
            [environ.core :refer [env]]
            [timeless.core :as core]
            [timeless.server :as svr]
            [ring.adapter.jetty :as jt]
            [ring.middleware.reload :refer [wrap-reload]])
  (:gen-class))

(defn handle-args
  "Function to parse the arguments to the main entry point of this project and
  do what it's asking. By the time we return, it's all done and over."
  [args app]
  (let [[params [action]] (cli args
             ["-p" "--port"     "Listen on this port" :default 8080 :parse-fn #(Integer. %)]
             ["-s" "--ssl-port" "Listen on this port" :default 8443 :parse-fn #(Integer. %)]
             ["-v" "--verbose" :flag true])
        quiet? (:quiet params)
        fixed? (:fixed params)
        port (Integer. (or (env :port) (:port params) 5000))]
    (cond
      (= "web" action)
		      (try
		        (jt/run-jetty app {:port port})
		        ; finally, close down the async jobs
		        (finally
		          ))
      :else
        (do
          (info "Welcome to Timeless!")
          (println "Welcome to Timeless")))))

(defn wrap-error-handling
  [func]
  (try
    (func)
    (catch Throwable t
      (.println System/err (str "Error in main: " t))
      (error t "Error in main")
      (throw t))))

(defmacro with-error-handling
  [& body]
  `(wrap-error-handling (fn [] ~@body)))

(defn -main
  "Function to kick off everything and clean up afterwards."
  [& args]
  (with-error-handling (handle-args args svr/app)))

(defn -dev-main
  "Development-time function to kick off everything and clean up afterwards.
  This version additionally wraps in the ring reload handler so
  that code changes are automatically loaded on each request when in development."
  [& args]
  (with-error-handling (handle-args args (wrap-reload #'svr/app {:dirs ["src" "checkouts"]}))))
