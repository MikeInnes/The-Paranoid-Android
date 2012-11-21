(ns util.spacers
  "Space apart code execution in a reliable, optimal
  and thread-aware way.")

(defn system-ms
  "Gives System.nanoTime in rounded milliseconds."
  [] (-> (System/nanoTime) (/ 1e6) Math/round))

(defn spacer
  "Create a new delay object, interval in ms."
  [interval] (atom [interval 0 0]))

(defn space
  "Will block until `interval` ms have passed since
  all other calls finished. Used by `spaced`."
  [spacer] (-> (swap! spacer
                 (fn [[interval last _]]
                   (let [now  (system-ms)
                         next (+ last interval)  ; Time of next call
                         wait (- next now)]      ; Time until next call
                     (if (> wait 0)              ; `next` might be in the past
                       [interval next wait]
                       [interval now     0]))))  ; in which case "next" is really now
               last Thread/sleep))

(defmacro spaced
  "`spaced` code blocks will not execute within `interval`
  milliseconds of each other, so can be used to space out
  API calls in an optimal and thread-aware way."
  [spacer & forms] `(do (space ~spacer) ~@forms))

(defn filter-map [f map]
  (into {} (filter f map)))

(defn memoize'
  "Like the original memoize, except that results expire
  after t milliseconds."
  [f timeout]
  (let [mem (atom {})]
    (fn [& args]
      (swap! mem #(filter-map
                    (fn [[_ [_ t]]]
                      (< (- (system-ms) t)
                         timeout))
                    %))
      (if-let [[result _] (get @mem args)]
        result
        (let [result (apply f args)]
          (swap! mem assoc args [result (system-ms)])
          result)))))

(defn apply-opts [f & args]
  (let [opts (last    args)
        args (butlast args)]
    (apply f args (reduce concat opts))))
