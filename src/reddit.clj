(ns reddit
  (:use      reddit.core util.spacers util.time)
  (:require [clojure.string :as str ]
            [cheshire.core  :as json]))

(def api-spacer (spacer 2000))
(defmacro api-call
  "Code blocks wrapped with `api-call` will not execute within
  two seconds of each other. All calls to the reddit api should
  use this macro. eg `(pmap #(api-call (println %)) (range 5))`
  takes 8 s, with 2 s between each call to `println`."
  [& forms] `(spaced api-spacer ~@forms))

;; -----------
;; URL makers.
;; -----------

(defmacro reddit
  "Macro, turns (reddit api eg) into 'http://www,reddit.com/api/eg'"
  [& rest] (str "http://www.reddit.com/" (str/join "/"  rest)))

(defn- parse-subreddits [names] (if (string? names) [names] names))

(defn subreddit
  "Links page for a given subreddit (string or vector)."
  [names]
  (let [names (parse-subreddits names)]
    (str (reddit) "r/" (str/join "+" names))))

(defn subreddit-comments
  "Comments page for a given subreddit (string or vector)."
  [names] (str (subreddit names) "/comments"))

(defn subreddit-links
  "Comments page for a given subreddit (string or vector)."
  [names] (str (subreddit names) "/new"))

;; --------------
;; Reddit objects
;; --------------

(defn comment? [thing] (= (:kind thing) :comment))
(defn link?    [thing] (= (:kind thing) :link   ))

;; --------------------
;; Interact with Reddit
;; --------------------

(defmacro with-user-agent
  [agent & code]
  `(binding [*user-agent* ~agent] ~@code))

(defn login
  "Returns a login object {:cookie/:modhash} that can be
  passed to the request functions."
  [user pass]
  (let [result (post (reddit api login)
                     :params {"user" user, "passwd" pass, "api_type" "json"})]

    {:name    user
     :cookies (result :cookies)
     :modhash (-> result :body (json/decode true) :json :data :modhash)}))

(defn me
  "Data about the currently logged in user."
  [login] (parse (get-json (reddit api me)
                           :login login)))

(defn items
  "Returns a lazy sequence of all items at the given
  url, including subsequent pages. Reddit usually
  limits this to ~1000. API calls spaced."
  ([url] (items url nil))
  ([url after]
    (lazy-seq
      (let [s (api-call (items-after after url 1000))]
        (if-not (empty? s)
          (concat s (items url (last s))))))))

(defn items-since
  "Takes `items` posted after the specified DateTime object."
  [date url] (take-while #(after? (% :time) date) (items url)))

(defn with-replies
  "Reload the comment/link with :replies data.
  Not considered reliable; it only loads one
  page, and will fail if used too often due to
  a 304 error."
  [thing]
  (let [comments (-> (thing :permalink) get-parsed second)]
    (cond
      (comment? thing) (first comments)
      (link?    thing) (assoc thing :replies comments)
      :else            thing)))

(defn reply-by?
  "Check if a link/comment has been replied to by a given account.
  Not reliable, see `with-replies`."
  [thing account]
  (some #(= (% :author) account) ((with-replies thing) :replies)))

(defn reply
  "Parent should be a link/comment object, reply is a string."
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

(defn vote
  "Vote :up, :down, or :none on a link/comment."
  [item direction login]
  (post (reddit api vote)
        :login login
        :params {:id  (item :name)
                 :dir (direction
                        {:up 1, :none 0, :down -1})}))
