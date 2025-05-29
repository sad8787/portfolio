function getAnswerGPT(question) {
    
    var $session = $jsapi.context().session;
    var new_message = {"role": "user", "content": question };
    var message_history = $session.message_history
    var max_tokens=4097;
    
    message_history.push(new_message);
    
    for (var i = 0; i < message_history.length; i++) {
        max_tokens = max_tokens - message_history[i].content.length;
    }
    if(max_tokens < 500){
        return "История поиска ChatGPT переполнена. Вы должны почистить ее."
    }
    var url = "https://api.openai.com/v1/chat/completions";
    var options = {
            dataType: "json",
            headers: {
                "Content-Type": "application/json",
                "Authorization": "Bearer " + $secrets.get("token_openai", "Токен OpenAI не найден")
            },
            body: {
                "model": "gpt-3.5-turbo",
                "messages": message_history,
                "max_tokens": max_tokens
            },
            timeout: 20000// sets timeout to 20 seconds  
    };
    
    var response = $http.post(url, options);
    var response_text = '';
    if (response.isOk) {
        response_text = response.data.choices[0].message.content;
        new_message = {"role": "assistant", "content": response_text };
        message_history.push(new_message);
        
    }
    else if (response.error.length > 0) {
        response_text = response.error;
        response_text += "  Попробуйте задать более конкретный вопрос"
        message_history = [];
    }
    else {
        response_text = "Не известная ошибка";}
        
    
    $session.message_history=message_history;
    return response_text ;
}