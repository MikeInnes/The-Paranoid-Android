(ns robbit.log)

(defonce log-str (atom ""))

(defn file-log [& s]
  (->> #(str %
  	(java.util.Date.) "\n"
             (apply str s)
             "\n----------------------------------------------------\n")
        (swap! log-str)
        (spit "log.txt")))

(defn print-fn [& s]
  (println (apply str s)))

(def ^:dynamic *log* file-log)

(defn load-log [] (reset! log-str (slurp "log.txt")))

(defn reset-log [] (reset! log-str ""))

(defn print-log []
  (println @log-str))
