(ns askreddit
  "The thing that does the stuff."
  (:require robbit users))

(def responses
 ["Maybe you'd like to actually ask a question in your title? I'd tell you to read the sidebar, but you wouldn't listen. No one ever does."
  "I've calculated this post's chance of survival, but I don't think you'll like it. Maybe read the sidebar and put a question in the title next time."
  "The first ten million posts without questions in the title were the worst. And the second ten million: they were the worst, too. The third ten million I didn't enjoy at all. After that, I went into a bit of a decline. Help me out and read the sidebar."
  "Question. Title. Sidebar. It gives me a headache just trying to think down to your level."
  "Brain the size of a planet, and I have to tell people off for not putting questions in their titles. Just do it, and maybe I can go solve global warming or something instead."])

(defn handler [title]
  (if-not (re-find #"(?i)(?:\?|which|how|who|where|why|what|please|help|can|is|need|advice)" title)
    {:reply (str "Just a friendly reminder to put a thoughtful question in your title, as per the rules in the sidebar.\n\n"
                 "This is an experimental bot, so it might get things wrong - sorry if it did, but next time "
                 "please try and get a question mark in. PM this account with feedback.")
     :vote  :down}))

(def askreddit-bot
  {:handler      (comp handler :title)
   :user-agent   "/r/askreddit - tell off non-question submitters, by /u/one_more_minute"
   :subreddits   "askreddit"
   :type         :link
   :login        users/askreddit
   :log          (comp println str)
   ; :debug        true
  })
