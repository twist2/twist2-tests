Data Store
==========

* In an empty directory initialize a project named "data_store" with the current language

Scenario data store refreshes after every scenario run.
-------------------------------------------------------

* Create a specification "newSpec" with the following contexts 
     |step text    |implementation        |
     |-------------|----------------------|
     |First context|"inside first context"|

* Create a scenario "newScenario" in specification "newSpec" with steps to read and write to datastore 
     |step text                   |key       |value                         |datastore type|
     |----------------------------|----------|------------------------------|--------------|
     |Store in scenario datastore |gauge_test|Some temporary datastore value|Scenario      |
     |Read from scenario datastore|gauge_test|                              |Scenario      |

* Execute the current project and ensure success

* Console should contain "Some temporary datastore value"


Spec data store persists data between scenario runs
---------------------------------------------------

* Create a specification "newSpec" with the following contexts 
     |step text    |implementation        |
     |-------------|----------------------|
     |First context|"inside first context"|
* Add tags "hadjka" to specification "newspec"
* Create a scenario "writeScenario" in specification "newSpec" with step to write to datastore 
     |step text              |key       |value                         |datastore type|
     |-----------------------|----------|------------------------------|--------------|
     |Store in spec datastore|gauge_test|Some temporary datastore value|Spec          |

* Create a scenario "readScenario" in specification "newSpec" with step to read from datastore 
     |step text          |key       |value|datastore type|
     |-------------------|----------|-----|--------------|
     |Read spec datastore|gauge_test|     |Spec          |

* Execute the current project and ensure success

* Console should contain "Some temporary datastore value"

Suite data store persists data between spec runs
------------------------------------------------

specs are read and run alphabetically, so we create specs in alphabetical order of names i.e. "first write spec", "second read spec"

* Create a specification "first write spec" with the following contexts 
     |step text    |implementation        |
     |-------------|----------------------|
     |First context|"inside first context"|

* Create a scenario "write scenario" in specification "first write spec" with step to write to datastore 
     |step text               |key       |value                         |datastore type|
     |------------------------|----------|------------------------------|--------------|
     |Store in suite datastore|gauge_test|Some temporary datastore value|Suite         |

* Create a specification "second read spec" with the following contexts 
     |step text    |implementation        |
     |-------------|----------------------|
     |First context|"inside first context"|

* Create a scenario "read Scenario" in specification "second read spec" with step to read from datastore 
     |step text           |key       |value|datastore type|
     |--------------------|----------|-----|--------------|
     |Read suite datastore|gauge_test|     |Suite         |

* Execute the current project and ensure success

* Console should contain "Some temporary datastore value"

