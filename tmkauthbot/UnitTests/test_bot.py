#from scripts.bot_utils import bot_utils_static

import unittest



class TestBotUtilsStatic(unittest.TestCase):
    

    def test_is_group_chat_false(self):
        target = __import__("bot_utils.py")
        self.message.chat.type = 'private'
        self.assertFalse(target.is_group_chat(self.message))

    def test_sum_tuple(self):
        self.assertEqual(sum((1, 2, 2)), 6, "Should be 6")

if __name__ == '__main__':
    unittest.main()



#import unittest
#from unittest.mock import Mock, patch
#from scripts.bot_utils import bot_utils_static  # Предположим, что ваш код находится в your_module.py
#from scripts.user_status import User_status
#from datetime import datetime

#class TestBotUtilsStatic(unittest.TestCase):

#    def test_is_group_chat_false(self):
#        self.message.chat.type = 'private'
#        self.assertFalse(bot_utils_static.is_group_chat(self.message))

    #def setUp(self):
    #    self.message = Mock()
    #    self.sso_params = Mock()
    #    self.bot = Mock()
    #    self.message_text = "Test Message"
    #    self.count_days_for_ban = 3

    #    # Создадим объект для имитации пользователя
    #    self.user = Mock()
    #    self.user.telegram_user_id = 1234
    #    self.user.status = User_status.confirmed.value
    #    self.user.date_in = datetime.today().date()
    #    self.user.user_name = "Test User"
    #    self.user.refresh_token = "Test Refresh Token"

    #def test_is_group_chat_true(self):
    #    self.message.chat.type = 'group'
    #    self.assertTrue(bot_utils_static.is_group_chat(self.message))

    #def test_is_group_chat_false(self):
    #    self.message.chat.type = 'private'
    #    self.assertFalse(bot_utils_static.is_group_chat(self.message))

    #def test_is_private_chat_true(self):
    #    self.message.chat.type = 'private'
    #    self.assertTrue(bot_utils_static.is_private_chat(self.message))

    #def test_is_private_chat_false(self):
    #    self.message.chat.type = 'group'
    #    self.assertFalse(bot_utils_static.is_private_chat(self.message))

    ## ... Напишите такие же тесты для других методов, аналогично тому, как это сделано выше ...

    #@patch('your_module.requests.post')
    #def test_get_access_token(self, mock_post):
    #    # Предположим, что requests.post возвращает объект ответа с кодом 200 и JSON телом ответа
    #    mock_response = Mock()
    #    mock_response.status_code = 200
    #    mock_response.json.return_value = {
    #        "access_token": "test_access_token",
    #        "expires_in": "test_expires_in",
    #        "refresh_token": "test_refresh_token"
    #    }
    #    mock_post.return_value = mock_response

    #    code = "test_code"
    #    result = bot_utils_static.get_access_token(code, self.sso_params)
    #    self.assertEqual(result.status_code, 200)
    #    self.assertEqual(result.json()["access_token"], "test_access_token")

    #@patch('your_module.bot_utils_static.get_refresh_token')
    #def test_check_user_set_not_confirmed(self, mock_get_refresh_token):
    #    # ... Реализуйте этот тест, следуя тому же подходу, что и в test_get_access_token ...


if __name__ == '__main__':
    unittest.main()
