The Paranoid Android
====================

Marvin, the paranoid android, is a depressed reddit bot. He currently services /r/scp by providing links to SCP articles whenever one is mentioned. And often when one isn't.

Marvin is powered by the [mynx](http://github.com/one-more-minute/mynx) library.

### Running Marvin

In order to run Marvin you'll need to provide a username and password, either by setting the `REDDIT_USER` and `REDDIT_PASSWORD` environment variables or by altering the `login!` call at the end of `scp.clj`. Assuming you have leiningen installed, you can then boot him up with:

```
lein run -m scp/start
```

If you're doing this, please remove all subreddits except for `sandboxtext` from the [code](https://github.com/one-more-minute/The-Paranoid-Android/blob/e2feed5a458dd444464fe62414c8ca70b71d036d/src/scp.clj#L122).
