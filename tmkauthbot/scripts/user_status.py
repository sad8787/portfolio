from enum import Enum

class User_status(Enum):
    confirmed = "confirmed"
    not_confirmed = "notconfirmed"
    baned = "baned"
    baned_for_ever = "baned for ever"