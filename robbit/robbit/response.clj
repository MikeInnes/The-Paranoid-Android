(ns robbit.response
  "Handlers for bot responses."
  (:use reddit robbit.log))

(def ^:dynamic *debug* false)

(defmulti handle-response
  "This is applied to each pair of the response map, dispatching
  based on key. eg if the response map contains `{:reply \"string\"}`,
  `(handle-response bot item \"string\")` is called, dispatching to
  :reply."
  (fn [bot item data] (:response-type (meta item))))

(defmethod handle-response :reply [bot {:keys [permalink body title time] :as item} text]
  (let [result (if-not *debug*
                 (reply item text (bot :login))
                 text)]
    (*log*
      permalink "\n"
      "Replied to by " (-> bot :login :name) "\n"
      result)))

(defmethod handle-response :vote [bot item direction]
  (when-not *debug*
    (vote item direction (bot :login))))
