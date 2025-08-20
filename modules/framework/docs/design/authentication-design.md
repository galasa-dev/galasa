# Authentication Design Notes

## Initial auth flow when user logs into the web UI
```mermaid
sequenceDiagram

    title Initial auth flow

    Actor User as User
    participant WebUI as Web UI
    participant AuthAPI as Auth API
    participant Dex as Dex

    User ->> WebUI : Navigates to the web UI

    activate WebUI
    WebUI->> AuthAPI: GET /auth?client_id=galasa-webui&callback_url=http://webui-hostname/callback
    
    activate AuthAPI
    note left of User: This GET /auth request uses the static client ID for the web UI that was configured into Dex.


    AuthAPI ->> Dex: GET /auth?client_id=galasa-webui&scope=...&state=somestate&redirect_uri=http://galasa-api/auth/callback

    activate Dex
    Dex ->> AuthAPI: Redirect to /auth/callback?code=someauthcode&state=somestate
    deactivate Dex

    AuthAPI ->> WebUI: Redirect to http://webui-hostname/callback?code=someauthcode

    deactivate AuthAPI
    note left of User : The redirect's location is the same "callback_url" provided in the initial GET /auth request.

    WebUI ->> AuthAPI: POST /auth (client_id, client_secret, code)
    activate AuthAPI
    AuthAPI ->> Dex: POST /token (client_id, client_secret, code)
    activate Dex
    Dex -> AuthAPI: Success response (JWT, refresh token)
    deactivate Dex
    AuthAPI -> WebUI: Success response (JWT, refresh token)
    deactivate AuthAPI

    WebUI --> User: Displays the web UI's landing page
    deactivate WebUI
```
## When the Web UI requests a new personal access token
```mermaid
sequenceDiagram

    title Authentication flow when requesting a new personal access token

    actor User
    participant WebUI as "Web UI"
    participant AuthAPI as "Auth API"
    participant Dex

    User ->> WebUI: Requests personal access token
    activate WebUI

    WebUI ->> AuthAPI: POST /auth/clients with "Authorization: Bearer <JWT>" header
    activate AuthAPI
    AuthAPI ->> AuthAPI: Check "Authorization" header contains a valid JWT
    AuthAPI ->> Dex: gRPC call to createClient()
    activate Dex
    Dex --> AuthAPI: Success response (client_id, client_secret)
    deactivate Dex
    AuthAPI --> WebUI: Success response (client_id, client_secret)
    deactivate AuthAPI

    WebUI ->> AuthAPI: GET /auth?client_id=myclient&callback_url=http://webui-hostname/callback
    activate AuthAPI
    note left of User : The following is identical to the initial authentication flow, but the client_id used will be the ID of the newly created Dex client.

    AuthAPI ->> Dex: GET /auth?client_id=myclient&scope=...&state=somestate&redirect_uri=http://galasa-api/auth/callback
    activate Dex
    Dex --> AuthAPI: Redirect to /auth/callback?code=someauthcode&state=somestate
    deactivate Dex
    AuthAPI --> WebUI: Redirect to http://webui-hostname/callback?code=someauthcode
    deactivate AuthAPI
    note left of User : The redirect's location is the same "callback_url" provided in the GET /auth request.

    WebUI ->> AuthAPI: POST /auth (client_id, client_secret, code)
    activate AuthAPI
    AuthAPI ->> Dex: POST /token (client_id, client_secret, code)
    activate Dex
    Dex --> AuthAPI: Success response (JWT, refresh token)
    deactivate Dex
    AuthAPI --> WebUI: Success response (JWT, refresh token)
    deactivate AuthAPI

    WebUI --> User: Displays personal access token details
    deactivate WebUI
```

## When the CLI logs-in using a personal access token
```mermaid
    sequenceDiagram

    title Authentication flow to get a new JWT for the Galasa CLI tool using a refresh token

    actor User
    participant GalasaCLI as "Galasa CLI"
    participant AuthAPI as "Auth API"
    participant Dex

    User ->> GalasaCLI: Runs "galasactl auth login"
    activate GalasaCLI

    GalasaCLI ->> AuthAPI: POST /auth (client_id, client_secret, refresh_token)
    activate AuthAPI

    AuthAPI ->> Dex: POST /token (client_id, client_secret, refresh_token)
    activate Dex
    Dex --> AuthAPI: Success response (JWT, refresh token)
    deactivate Dex
    AuthAPI --> GalasaCLI: Success response (JWT, refresh token)
    deactivate AuthAPI

    GalasaCLI --> User: Stores the JWT in GALASA_HOME/bearer-token.json
    deactivate GalasaCLI
```

## When the user logs in to the web UI...
```mermaid
sequenceDiagram
    actor toby as "Toby (Tester)" 
    participant webUI as "Galasa Web UI" 
    participant Dex as Dex
    participant db as "Configured Users DB" 

    toby ->> webUI: Get request
    webUI ->> toby: Logon challenge
    toby -->> webUI: User id and password
        webUI ->> Dex: "Authentication request (user login)"
            Dex ->> db: Authentication request
            db -->> Dex :  User is valid Response
        Dex -->> webUI: OK
    webUI -->> toby: main web app UI
```

## When the web UI creates a personal access token
```mermaid
sequenceDiagram
    actor toby as "Toby (Tester)"
    participant webUI as "Galasa Web UI" 
    participant auth as "Auth Service"
    participant Dex as Dex

    toby ->> webUI: Add personal access token
            webUI ->> auth : Add personal access token 
                    auth ->> Dex : Add personal acces token as valid
                    Dex -->> auth : OK
            auth -->> webUI : OK
    webUI -->> toby : Personal Access Token appears on panel
```

## When the command line logs in
```mermaid
sequenceDiagram
    actor toby as "Toby (Tester)"
    participant cli as "Command line tool"
    participant env as "Environment"
    participant auth as "Auth Service"
    participant Dex as Dex


    toby->> cli: login(personal access token)
    cli->> auth : authenticate(personal access token) 
    auth->> Dex : Is this personal access tokeb valid>
    Dex -->> auth:OK
    auth ->> Dex : Allocate a JWT token
    Dex -->> auth :OK (JWT token,refresh token)
    auth -->> cli:OK (JWT token, refresh token)
    cli ->> env: set GALASA_BEARER_TOKEN
    cli ->> env: set GALASA_BEARER_REFRESH_TOKEN
    cli -->> toby: OK
```

## When the CLI submits a test to the server
```mermaid
sequenceDiagram
    actor toby as "Toby (Tester)" 
    participant cli as "CLI tool" 
    participant env as "Environment"     
    participant runsService as "Runs Service"

    toby->> cli: runs submit
            activate cli
            cli->> env: read GALASA_BEARER_TOKEN

            cli->>   runsService : post(runs,bearer-token) 
                    activate runsService
                    runsService->> Dex : Is this bearer token valid ?
                    Dex -->> runsService :OK

    note over runsService: The test runs can now go ahead.

                    runsService -->> cli:OK
                    deactivate runsService


    cli -->> toby: OK
    deactivate cli
```