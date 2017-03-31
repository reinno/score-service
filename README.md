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
curl -i  -X POST  -H "Content-Type:application/json" -d '[{"hotelId": 2, "countryId": 2}]' http://127.0.0.1:8002/api/v1/score
HTTP/1.1 200 OK
Server: akka-http/2.4.10
Date: Fri, 31 Mar 2017 10:16:54 GMT
Content-Type: application/json
Content-Length: 27

[{"hotelId":2,"score":3.0}]
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
curl -i  -X POST  -H "Content-Type:application/json"  http://127.0.0.1:8002/api/v1/rule/special-hotel/enable
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
curl -i  -X POST  -H "Content-Type:application/json"  http://127.0.0.1:8002/api/v1/rule/special-hotel/disable
```

