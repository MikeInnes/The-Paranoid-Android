(ns bots
  (:require [monger.core :as mg]
             robbit)
  (:use scp karma-police))

(defn mongo-connect! []
  (if-let [url (System/getenv "MONGOHQ_URL")]
    (mg/connect-via-uri! url)
    (do
      (mg/connect!)
      (mg/set-db! (mg/get-db "local")))))

(defn start [& [file-log debug]]
  (robbit/with-log (if file-log robbit.log/file-log robbit.log/print-fn)
    (binding [robbit.response/*debug* debug])
      (robbit/start scp-bot      :scp-bot)
      #_(robbit/start karma-police :karma-police)))
  nil)

(defn stop []
  (robbit/stop :scp-bot)
  (robbit/stop :karma-police))
