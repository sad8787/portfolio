
//import { jwt } from "jose"; 
function getAnswerYandexGPT(question) {
    var $session = $jsapi.context().session;
    var new_message = {"role": "user", "text": question };
    var message_history = $session.message_history;
    var max_tokens=4097;
    
    message_history.push(new_message);
    
    for (var i = 0; i < message_history.length; i++) {
        var Array = message_history[i].text;
        max_tokens = max_tokens - Array.length;
    }
    if(max_tokens < 500){
        return "История поиска ChatGPT переполнена. Вы должны почистить ее."
    }
    
    var token = $secrets.get("token_yandexGPT", "Токен token_yandexGPT не найден")
    //var token ="t1.9euelZrIzM7Kx86SkJCQjZqJzpfLi-3rnpWayJecyMrIys-Px5KUmseJmonl8_cxQwJV-e86NxJl_d3z93Fxf1T57zo3EmX9zef1656VmorMkZeMjo6ajoyTypyUyp7I7_zF656VmorMkZeMjo6ajoyTypyUyp7I.WJiIUJc8ehM5hDj2sCvVgqcfS2w4tlUGnf8vufBlWp3Duj7FvJ7wkRYmKX_O_LHJg-Ku31gmuAWI5NI9AVXVCg";
    var folder_id = "b1gk1aug953814odlreo";
    var url = "https://llm.api.cloud.yandex.net/llm/v1alpha/chat";
    
    var options = {
            dataType: "json",
            headers: {
                "Authorization": "Bearer " + token,
                "x-folder-id" : folder_id
            },
            body: {
                "model": "general",
                "generationOptions": {
                    "partialResults": true,
                    "temperature": 0.5,
                    "maxTokens": max_tokens
                    },
                "messages": message_history
            },
            timeout: 20000// sets timeout to 20 seconds  
    };
    
    var response = $http.post(url,options);
    if(response.status == 200){
        var text = JSON.stringify(response.data.result.message.text);
        new_message = {"role": "assistant", "content": text };
        message_history.push(new_message);
        return text;
    }
    return "error:" + response.error ;
   
}


function base64url(source) {
  // Encode in classical base64
 
  var encodedSource = CryptoJS.enc.Base64.stringify(source);

  // Remove padding equal characters
  encodedSource = encodedSource.replace(/=+$/, '');

  // Replace characters according to base64url specifications
  encodedSource = encodedSource.replace(/\+/g, '-');
  encodedSource = encodedSource.replace(/\//g, '_');

  return encodedSource;
}
function ecode_token(){
    var header = {
        "alg": "HS256",
        "typ": "JWT"
    };
    //var jwt = require('jsonwebtoken');
    var service_account_id = "ajeoap69e6v1r7ndle4k";//"<идентификатор_сервисного_аккаунта>";
    var key_id = "ajeq9h46bpt247hdj6tv";// "<идентификатор_открытого_ключа>"; // ID ресурса Key, который принадлежит сервисному аккаунту.


    //close_key
    var private_key = $secrets.get("close_key", "close_key не найден")
    //var privatee = fs.readFileSync(close_key, 'utf8');//<файл_закрытого_ключа>
    //var private_key = privatee; // Чтение закрытого ключа из файла.

    var now = Math.floor(Date.now() / 1000);
    var payload = {
        'aud': 'https://iam.api.cloud.yandex.net/iam/v1/tokens',
        'iss': service_account_id,
        'iat': now,
        'exp': now + 360
    };
    
    j

    var stringifiedHeader = CryptoJS.enc.Utf8.parse(JSON.stringify(header),payload);
    var encodedHeader = base64url(stringifiedHeader);

    

    var token = encodedHeader; 
    return token;
    
}


function IAM_Token(){ 
    
    var service_account_id = "ajeoap69e6v1r7ndle4k";//"<идентификатор_сервисного_аккаунта>";
    var key_id = "ajeq9h46bpt247hdj6tv";// "<идентификатор_открытого_ключа>"; // ID ресурса Key, который принадлежит сервисному аккаунту.


    //close_key
    var private_key = $secrets.get("close_key", "close_key не найден")
    //var privatee = fs.readFileSync(close_key, 'utf8');//<файл_закрытого_ключа>
    //var private_key = privatee; // Чтение закрытого ключа из файла.

    var now = Math.floor(Date.now() / 1000);
    var payload = {
        'aud': 'https://iam.api.cloud.yandex.net/iam/v1/tokens',
        'iss': service_account_id,
        'iat': now,
        'exp': now + 360
    };
    //var encoded_token = jwt.encode(
      //  payload,
      //  private_key,
      //  algorithm = 'PS256',
      //  headers = { 'kid': key_id });



// Формирование JWT.
    var encoded_token = JOSE.jsonwebtoken.Encode(
        payload,
        private_key,
        { algorithm: 'PS256', keyid: key_id }
        );
        
    return encoded_token;
}