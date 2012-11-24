(ns karma-police
  (:use karma-police.karmadecay reddit reddit.format)
  (:require users))

(declare karma-police)

; (def test-url "http://www.reddit.com/r/WTF/comments/13euma/parenting_youre_doing_it_wrong/")

(defn reposts [url]
  (map link-from-url (repost-urls url)))

(defn top-comments [links]
  (->> links
       (map first-reply)
       (filter identity)
       (remove deleted-comment?)
       (remove #(author? % (-> karma-police :login :name)))))

(defn top-comment [links]
  (->> links top-comments
       (sort-by :score)
       last))

(defn top-comment-formatted [links]
  (if-let [{:keys [body author permalink]} (top-comment links)]
    (str body "\n\n"
         (italic
           (str "~ " (hyperlink author permalink))))))

(defn count-string [n]
  (condp = n
    1 "once"
    2 "twice"
    3 "thrice"
    (str n " times")))

; (defn link-list [links]
;   (clojure.string/join "\n"
;     (map #(str "* " (hyperlink (:title %) (:permalink %))) links)))

(defn link-reply [url]
  (when-let [reposts (-> url reposts seq)]
    (when-let [top-comment (top-comment-formatted reposts)]
      {:reply (paragraphs
                top-comment
                line
                (italic
                  (superscript
                    "This image has been submitted "
                    (hyperlink (count-string (count reposts)) (karmadecay-url url))
                    " before. Above is the previous top comment.")))
      :vote :up})))

(def karma-police
  {:handler      (comp link-reply :permalink)
   :user-agent   "Top Comment Bot by /u/one_more_minute"
   :subreddits   ["funny" "wtf" "pics"]
   :type         :link
   :login        users/top-comment
   :log          (comp println str)
   :interval     2
   ; :debug        :true
  })
