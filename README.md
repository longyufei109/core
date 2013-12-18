Core Module
==============

A set of core classes containing various utilities and base classes used to build other modules and kickstart project development.

Basically, this is my "commons" set of classes that I seem to be writing over and over again.  In here, we have everything from the cheap tools
to do basic String -> conversions, http connections, database connectivity, servlet catchers, json writers and so on.  Most of these are a result
finding something that does what I want (like the Jackson JSON parser) and applying it to some base class (com.subdigit.data.BaseModel) so that I
have something quick and easy to reuse to build other projects (like the [Authentication Module](https://github.com/subdigit/authentication).

For example, the com.subdigit.startup.morphia package has an abstract MorphiaStartup class which uses the DataConnectorConfiguration class (which extends
the BaseConfigurationReader) to read property information about the MongoDB instance from the dataconnectorconfiguration.properties file during its instantiation.

Thus all you need to do is to create a class that extends MorphiaStartup, fill in the setup() method with your all needs like prepopulating the DB or whatever
else you need for your project upon startup. 

When you instantiate the class, the system connects to your MongoDB instance and populates the necessary mongo, datastore, morphia variables in the static
MorphiaDataConnector class.

So when all is said and done, you instantiate your implementation of MorphiaStartup and then access MorphiaDataConnector.getInstance().getDatastore()
to pass that along to your Morphia BasicDAO objects.  Which incidentally, you wont have to worry about if you extend the com.subdigit.data.BasicAccessor class.

Basically I'm trying to build an opinionated, prepackaged set of frameworkish classes that enables me to do things uniformly in a particular manner each time.

I'm hoping with the growth of this class, I wont have to worry about json output, morphia connections, error message passing, common String conversions, incoming
requests differences (because one day I use servlets, the next day I use the Play framework).

We'll see how far I can get...

Feel free to take a browse around and try it out.  I know, docs and unit tests are lacking, but I'll get to them as time passes.
