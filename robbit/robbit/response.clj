(ns robbit.response
  "Handlers for bot responses.")

(def ^:dynamic *debug*)

(defmulti handle-response
  "This is applied to each pair of the response map, dispatching
  based on key. eg if the response map contains `{:reply \"string\"}`,
  `(handle-response bot item \"string\")` is called, dispatching to
  :reply."
  (fn [bot item data] (:response-type (meta item))))

(defmethod handle-response :reply [bot item text]
  (let [result (if-not (bot :debug)
                 (reply item text (bot :login))
                 :debug)]
    (update-last-run bot (item :time))
    ((bot :log)
      (item :permalink) "\n"
      (item (if (comment? item) :body :title)) "\n"
      "\n"
      "Reply by " (-> bot :login :name) ":\n"
      text "\n"
      result)))

(defmethod handle-response :vote [bot item direction]
  (when-not (bot :debug)
    (vote item direction (bot :login))))
