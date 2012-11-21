(ns reddit
  (:use      reddit.core util.spacers util.time)
  (:require [clojure.string :as str ]
            [cheshire.core  :as json]
            [reddit.url :refer (reddit)]))

(def api-spacer (spacer 2000))
(defmacro api-call
  "Code blocks wrapped with `api-call` will not execute within
  two seconds of each other, eg
      `(pmap #(api-call (println %)) (range 5))`
  takes 8 s, with 2 s between each call to `println`. All calls
  to the reddit api should use this macro."
  [& forms] `(spaced api-spacer ~@forms))

;; --------------
;; Authentication
;; --------------

(defmacro with-user-agent
  [agent & code]
  `(binding [*user-agent* ~agent] ~@code))

(defn login
  "Returns a login object {:name :cookie :modhash}
  for passing to the request functions."
  [user pass]
  (let [result (post (reddit api login)
                     :params {"user" user, "passwd" pass, "api_type" "json"})]

    {:name    user
     :cookies (result :cookies)
     :modhash (-> result :body (json/decode true) :json :data :modhash)}))

;; ----------------
;; Retreiving Items
;; ----------------

(defn items-after
  "Loads 1 page of the links/comments after the given one."
  [item url limit]
  (get-parsed url
              :params {:limit limit
                       :after (:name item)
                       :sort  "new"}))

(defn items
  "Returns a lazy sequence of all items at the given
  url, including subsequent pages. API calls spaced."
  ([url] (items url nil))
  ([url after]
    (lazy-seq
      (let [s (api-call (items-after after url 1000))]
        (if-not (empty? s)
          (concat s (items url (last s))))))))

; Get rid of clj-time
(defn items-since
  "Takes `items` posted after the specified DateTime object."
  [date url] (take-while #(after? (% :time) date) (items url)))

;; --------------
;; Links/comments
;; --------------

(defn comment? [thing] (= (:kind thing) :comment))
(defn link?    [thing] (= (:kind thing) :link   ))

(defn link-from-url [url]
  (let [data     (get-parsed url)
        link     (ffirst data)
        comments (second data)]
    (assoc link :replies comments)))

(defn first-reply [thing]
  (-> thing :replies first))

(defn with-replies
  "Reload the comment/link (e.g. from `items`)
  with :replies data. Only loads one page."
  [thing]
  (let [data     (-> thing :permalink get-parsed)
        link     (ffirst data)
        comments (second data)]
    (cond
      (comment? thing) (-> thing (merge (first comments)))
      (link?    thing) (-> thing (merge link) (assoc :replies comments))
      :else            thing)))

(defn reply
  "Parent should be a link/comment object, reply is a string.
  Returns a keyword indicating either successfully `:submitted`
  or an error."
  [parent reply login]
  (let [response (post (reddit api comment)
                       :login login
                       :params {:thing_id (parent :name)
                                :text     reply})]
    (condp re-find (response :body)
      #"contentText"                         :submitted
      #".error.RATELIMIT.field-ratelimit"    :rate-limit
      #".error.USER_REQUIRED"                :user-required
      #".error.DELETED_COMMENT.field-parent" :parent-deleted
      (response :body))))

; (defn reply-by?
;   "Check if a link/comment has been directly replied to by a given
;   account. Not reliable, see `with-replies`."
;   [thing account]
;   (some #(= (% :author) account) ((with-replies thing) :replies)))

(defn vote
  "Vote :up, :down, or :none on a link/comment."
  [item direction login]
  (post (reddit api vote)
        :login login
        :params {:id  (item :name)
                 :dir (direction
                        {:up 1, :none 0, :down -1})}))

;; -----
;; Users
;; -----

(defn me
  "Data about the currently logged in user."
  [login] (parse (get-json (reddit api me)
                           :login login)))
