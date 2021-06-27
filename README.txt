[the main assumptions are given in the pdf file in the resources]

All data are provided from http://api.nbp.pl/api/exchangerates/rates/C/<currency>/<date>

The cantor converts currency amounts. Response contains
convert amount and message. Request has to provide four variables: currency input, currency output, method of transaction and
amount. I have done two http method (get/post), both can handle this case.
1. Template of request get method (recommended, because url shows parameters):
http://localhost:8080/api?currencyInput=<currency>&currencyOutput=<currency>&amount=<amount>
2. Template of request post method
Headers:
name: Content-Type          value: application/json

Request Body:
name: currencyInput         value: <currency>
name: currencyOutput        value: <currency>
name: amount                value: <amount>
