There are some features in the Maven Docbkx Plugin that perhaps could be deprecated. Among the things I would like to get rid of are:

### Calling an ant task from the plugin ###

It's awkward. It may be necessary at times, but I don't feel that this plugin should compensate for the fact that Maven does not allow you to break out of its harness of the different build phases in an easy way.

Are there any other things we could do without? If so, let us know by voting for the issues.