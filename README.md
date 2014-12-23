#Flatwhite

##What is Flatwhite?
Flatwhite is the back-end component to [Knotis](http://knot.is), an asynchronous messaging platform. Built in MongoDB and Play (Scala flavor), using ReactiveMongo, Flatwhite is the pure development sugar. Directions for getting Flatwhite up and running are below.

##How do I get Flatwhite running locally?
1. Be sure you have [Play](https://www.playframework.com/) installed and a local instance of [MongoDB](http://www.mongodb.org/) running on port 27017 (the default port). Trust me, it's webscale. For the MongoDB database, you'll need to have a collection created called `flatwhite`, along with a username and password assigned to it. In the repository, and in any further development environments, the username/password pair will be `flatwhite`/`gibraltar`, as indicated in this repository's `application.conf` file.
2. Clone this repository.
3. Run `play run` from the command line, in the top level of the cloned repository. This will begin the build process (as specified in `build.sbt`), and deploy the Play application locally.
4. Upon completion of the build step, you'll find the app running at the specified URL & port. This is by default `localhost:9000`.

##How do I develop within Flatwhite?
1. Follow the above directions for cloning this repository and getting Flatwhite running locally.
2. Be sure that your repository is up to date using `git pull`.
3. Make the appropriate changes, and submit a pull request to this repository via your favorite Git client or via command line. If you're unfamiliar with Git, use this for a great tutorial and introduction to the Git workflow: [Git School](https://try.github.io/).
4. Upon review, discussion, and approval from a repository administrator, your pull request will be merged into `master`.
5. Congratulations! And thanks for the commit.
