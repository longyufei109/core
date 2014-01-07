Core Module
==============

A set of core classes containing various utilities and base classes used to build other modules and kickstart project development.

Basically, this is my "commons" set of classes that I seem to be writing over and over again.  In here, we have everything from the cheap tools
to do basic String -> conversions, http connections, database connectivity, servlet catchers, json writers and so on.  Most of these are a result
finding something that does what I want (like the Jackson JSON parser) and applying it to some base class (com.subdigit.data.BaseModel) so that I
have something quick and easy to reuse to build other projects (like the [Authentication Module](https://github.com/subdigit/authentication)).

For example, the com.subdigit.startup.morphia package has an abstract MorphiaStartup class which uses the DataConnectorConfiguration class (which extends
the BaseConfigurationReader) to read property information about the MongoDB instance from the dataconnectorconfiguration.properties file during its instantiation.

Thus all you need to do is to create a class that extends MorphiaStartup, fill in the setup() method with all your setup needs like prepopulating the DB or whatever
else you need for your project upon startup. 

When you instantiate the class, the system connects to your MongoDB instance and populates the necessary mongo, datastore, morphia variables into the statically accessible 
MorphiaDataConnector class.

So when all is said and done, you instantiate your implementation of MorphiaStartup and then access MorphiaDataConnector.getInstance().getDatastore()
to pass that along to your Morphia BasicDAO objects.  Which incidentally, you wont have to worry about if you extend the com.subdigit.data.BasicAccessor class.

Basically I'm trying to build an opinionated, prepackaged set of frameworkish classes that enables me to do things uniformly in a particular manner each time.

I'm hoping with the growth of this project, I wont have to worry about json output, morphia connections, error message passing, common String conversions, incoming
request differences (because one day I use servlets, the next day I use the Play framework).

We'll see how far I can get...

Feel free to take a browse around and try it out.  I know, docs and unit tests are lacking, but I'll get to them as time passes.


So, here's a taste of what's here:
com.subdigit.broker
-------------------
Brokers take into account the request and response of a particular framework and give you a simplified interface to extract things like parameters and do things like redirects.
An example of a Servlet broker is in the .servlet sub package, which is what you'll implement in a J2EE environment.  I'll have one for the Play Framework eventually too.

com.subdigit.data
-------------------
The data package contains the base object and manipulation classes.  At the moment, they are all Morphia based.  The point is to extend off of these to create
classes specific to your needs.
* BaseModel -> A simple Morphia model to use as a starting point.  Comes with JSON output based on Jackson to be leveraged in a WebResult class
* BaseAccessor -> Extends Morphia's BasicDAO and prepares it with the Morphia instance gotten from the MorphiaDataConnector.
* BaseManager -> Business logic goes here.  If you need to manipulate the BaseModel in anyway, you do it at this level.  Offers some built in calls to make life easier.
* BaseService -> CRUD routines for the Model.  Calls the Manager appropriately and records the transactions of what happened.
* BaseInterceptor -> The final frontier between the code and the web.  Translates the incoming request into a common language and reports back results via a unified object.

com.subdigit.connector
-------------------
Connection to the local datastore.  Has classes like MorphiaDataConnector which is configured to read from a property file to instantiate the Morphia instance.

com.subdigit.data.result
-------------------
The returned results from the service classes to keep track of what happened during the call.

com.subdigit.request
-------------------
Request objects meant to be used instead of passing in parameters.  Ability to read directly from the incoming HttpServletRequest object to figure out the parameters,
what they mean, and what exactly is being requested.

com.subdigit.result
-------------------
Corresponding result object to the request object.  Outputs the final object(s) returned as well as status indicators.  Knows how to output content in the requested format (like json)
based on the annotations on the BaseModel and other built in configurations.

com.subdigit.startup
-------------------
Classes meant to be called at the start of a project to prepare it.  Includes the triggers needed to kick start the MongoDB/Morphia instance.

com.subdigit.utilities
-------------------
A collection of useful tools like converters and http server connection classes.


Maven Repository
-------------------
For now, this repository is hosted on GitHub as:

    <repositories>
      <repository>
        <id>com.subdigit.core</id>
        <url>https://raw.github.com/subdigit/core/mvn-repo/</url>
        <snapshots>
          <enabled>true</enabled>
          <updatePolicy>always</updatePolicy>
        </snapshots>
      </repository>
    </repositories>

