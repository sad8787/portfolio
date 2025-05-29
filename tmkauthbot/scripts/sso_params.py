class SSO_params:
    # конструктор класса
    def __init__(self, auth_endpoint, token_endpoint, userinfo_endpoint, redirect_uri, token_introspect, client_id, client_secret):
        self.auth_endpoint = auth_endpoint
        self.token_endpoint = token_endpoint
        self.userinfo_endpoint = userinfo_endpoint
        self.redirect_uri = redirect_uri
        self.token_introspect = token_introspect
        self.client_id = client_id
        self.client_secret = client_secret
