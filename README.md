# Score Service

[![Build Status](https://travis-ci.org/reinno/score-service.svg?branch=master)](https://travis-ci.org/reinno/score-service)


## How to
### Build
```bash
cd ./score-service
sbt assembly
java -jar ./target/scala-2.11/score-service-1.0.jar
```

### Add a Rule
- add a new rule actor extend RuleService
- add the rule actor props func to RuleService Props Factory
- add rule configuration


## Restful API
  
### Score

* **URL**

  /api/v1/score

* **Method:**
  
  `POST`
  
*  **URL Params**

   N/A

* **Data Params**

List of structure

|name     |type  |mandatory|description
|---------|------|---------|-----------
|hotelId  |Int   |yes      |
|countryId|Int   |yes      |

* **Success Response:**

  * **Code:** 200 <br />
    **Content:** 
    
List of structure

|name     |type  |mandatory|description
|---------|------|---------|-----------
|hotelId  |Int   |yes      |
|score    |Double|yes      |
 
* **Error Response:**

  * **Code:** 500 Internal failure <br />

  
* **Sample Call:**

```bash

```

### Rule

* **URL**

  /api/v1/rule/{rule_name}/enable

* **Method:**
  
  `POST`
  
*  **URL Params**

|name     |type  |mandatory|description
|---------|------|---------|-----------
|rule_name|string|yes      |

* **Data Params**

N/A

* **Success Response:**

  * **Code:** 200 <br />
 
* **Error Response:**

  * **Code:** 404 Rule not found <br />

  
* **Sample Call:**

```bash

```


* **URL**

  /api/v1/rule/{rule_name}/disable

* **Method:**
  
  `POST`
  
*  **URL Params**

|name     |type  |mandatory|description
|---------|------|---------|-----------
|rule_name|string|yes      |

* **Data Params**

N/A

* **Success Response:**

  * **Code:** 200 <br />
 
* **Error Response:**

  * **Code:** 404 Rule not found <br />

  
* **Sample Call:**

```bash

```

