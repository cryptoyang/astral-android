# User interactions

Probably there are endless ways of possible user interactions with the system depending on the
context, but hopefully most of them can be formalized and associated with one of a few general types
of intentions. Let's specify some possible user interactions, this should help with further
formalization:

* List contacts.
* Play $album on $device
* Play $video on $device
* Display &image on $device
* Send message to $contact.
* Notify me about new message from $contact.
* Scan directory.
* ...

# Intention

Assume that interaction between user and system becomes from the user intention. The intention is
telling what user want's from the system.

Consider following types of intentions:

* User wants to obtain information from the system,
    * as soon as possible.
    * when something occurs in the system,
        * once.
        * until user will stop subscription.
* User wants to perform some action on the system,
    * for adding some data to the system.
    * for running a task.
  
On some layer of abstraction the intention is equal to function.

# Parametrization

Any intention can require parameters if needed. The parameters can be represented by simple types
but also by complex structures. Depending on situation different method for collecting a data from
user will be needed.

## Data source

We can specify two different sources of data for parameters.

* Internal - When the application already knows the data that can be used as argument. For example
  the GUI can display prompt with a list for visual selection.
* External - When the application can't generate options to select and the user have to provide for
  example a text data using keyboard or voice.

## Processes

When the process of collecting the data is long or complicated probably it can be represented by
a complex structure like a list or tree. For example the list of different arguments for steps based
processes and the tree if the process requires a forks.

From technical perspective the specific group of api function calls organized in list or tree is
used to collect a data required to finalize the process.

# Formalization

According to preceding paragraphs let's try to formalize the model.

From the system perspective, the user can:

* Call a function,
    * for getting the data.
    * for performing some action, with optional result.
* Register and unregister a listeners,
    * for getting the data.
    * for performing some action, with optional result.

Both actions optionally can require parameters.

The user have to provide parameters, but the system can propose possible options to select if there
are any already known or possible to predict. Other words, any data that is served by system can by
also be used as a parameter data for the system calls, so the system should be able to automatically
propose it as an option to select for the user.

In formal way the interaction can be described as follows:

* What should be done.
* Optionally, how it should be done.
* Where send the results.
* What type of result could be expected.
    * Consider, attaching info about side effects as mandatory.

Intentionally it isn't different than calling a function with optional parameters for performing an
action and/or getting a results. The user intention should be easy mappable into a function call or
an RPC message. 
