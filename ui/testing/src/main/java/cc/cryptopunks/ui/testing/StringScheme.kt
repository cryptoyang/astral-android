package cc.cryptopunks.ui.testing

val openRpcSchemeJson
    get() = """
{
  "openrpc": "1.2.6",
  "info": {
    "title": "placeholder",
    "version": "0"
  },
  "methods": [
    {
      "name": "Messenger.GetContacts",
      "description": "```go\nfunc (m Messenger) GetContacts(reply chan []Contact) error {\n\treply \u003c- []Contact{{Id: ContactId{\"1\"}, Name: \"Joe\"}, {Id: ContactId{\"2\"}, Name: \"Bob\"}}\n\treturn nil\n}\n```",
      "summary": "",
      "paramStructure": "by-position",
      "params": [],
      "result": {
        "name": "reply",
        "description": "chan []Contact",
        "summary": "",
        "schema": {
          "items": [
            {
              "$schema": "http://json-schema.org/draft-04/schema",
              "$ref": "github.com/cryptopunkscc/astrald/service/test/Contact"
            }
          ],
          "type": [
            "array"
          ]
        },
        "required": true,
        "deprecated": false
      },
      "deprecated": false,
      "externalDocs": {
        "description": "Github remote link",
        "url": "https://github.com/cryptopunkscc/astrald/blob/master/service/test/serve.go#L19"
      }
    },
    {
      "name": "Messenger.GetMessages",
      "description": "```go\nfunc (m Messenger) GetMessages(id ContactId, reply chan ChatScreen) error {\n\treturn nil\n}\n```",
      "summary": "",
      "paramStructure": "by-position",
      "params": [
        {
          "name": "id",
          "description": "ContactId",
          "summary": "",
          "schema": {
            "$schema": "http://json-schema.org/draft-04/schema",
            "$ref": "github.com/cryptopunkscc/astrald/service/test/ContactId"
          },
          "required": true,
          "deprecated": false
        }
      ],
      "result": {
        "name": "reply",
        "description": "chan ChatScreen",
        "summary": "",
        "schema": {
          "$schema": "http://json-schema.org/draft-04/schema",
          "$ref": "github.com/cryptopunkscc/astrald/service/test/ChatScreen"
        },
        "required": true,
        "deprecated": false
      },
      "deprecated": false,
      "externalDocs": {
        "description": "Github remote link",
        "url": "https://github.com/cryptopunkscc/astrald/blob/master/service/test/serve.go#L38"
      }
    },
    {
      "name": "Messenger.GetOverview",
      "description": "```go\nfunc (m Messenger) GetOverview(reply chan []OverviewItem) error {\n\treturn nil\n}\n```",
      "summary": "",
      "paramStructure": "by-position",
      "params": [],
      "result": {
        "name": "reply",
        "description": "chan []OverviewItem",
        "summary": "",
        "schema": {
          "items": [
            {
              "$schema": "http://json-schema.org/draft-04/schema",
              "$ref": "github.com/cryptopunkscc/astrald/service/test/OverviewItem"
            }
          ],
          "type": [
            "array"
          ]
        },
        "required": true,
        "deprecated": false
      },
      "deprecated": false,
      "externalDocs": {
        "description": "Github remote link",
        "url": "https://github.com/cryptopunkscc/astrald/blob/master/service/test/serve.go#L17"
      }
    },
    {
      "name": "Messenger.SendMessage",
      "description": "```go\nfunc (m Messenger) SendMessage(Id ContactId, Text string) error {\n\treturn nil\n}\n```",
      "summary": "",
      "paramStructure": "by-position",
      "params": [
        {
          "name": "Id",
          "description": "ContactId",
          "summary": "",
          "schema": {
            "$schema": "http://json-schema.org/draft-04/schema",
            "$ref": "github.com/cryptopunkscc/astrald/service/test/ContactId"
          },
          "required": true,
          "deprecated": false
        },
        {
          "name": "Text",
          "description": "string",
          "summary": "",
          "schema": {
            "type": [
              "string"
            ]
          },
          "required": true,
          "deprecated": false
        }
      ],
      "result": null,
      "deprecated": false,
      "externalDocs": {
        "description": "Github remote link",
        "url": "https://github.com/cryptopunkscc/astrald/blob/master/service/test/serve.go#L45"
      }
    }
  ],
  "components": {
    "schemas": {
      "github.com/cryptopunkscc/astrald/service/test/ChatScreen": {
        "type": "object",
        "properties": {
          "Contact": {
            "$ref": "github.com/cryptopunkscc/astrald/service/test/Contact",
            "$schema": "http://json-schema.org/draft-04/schema"
          },
          "Messages": {
            "type": "array",
            "items": {
              "$ref": "github.com/cryptopunkscc/astrald/service/test/Message",
              "$schema": "http://json-schema.org/draft-04/schema"
            }
          },
          "Messages_add": {
            "type": "array",
            "items": {
              "$ref": "github.com/cryptopunkscc/astrald/service/test/Message"
            }
          },
          "Messages_remove": {
            "type": "array",
            "items": {
              "$ref": "github.com/cryptopunkscc/astrald/service/test/MessageId"
            }
          }
        },
        "additionalProperties": false
      },
      "github.com/cryptopunkscc/astrald/service/test/Contact": {
        "type": "object",
        "properties": {
          "Id": {
            "$ref": "github.com/cryptopunkscc/astrald/service/test/ContactId",
            "$schema": "http://json-schema.org/draft-04/schema"
          },
          "Name": {
            "type": "string"
          }
        },
        "additionalProperties": false
      },
      "github.com/cryptopunkscc/astrald/service/test/ContactId": {
        "type": "object",
        "properties": {
          "Value": {
            "type": "string"
          }
        },
        "additionalProperties": false
      },
      "github.com/cryptopunkscc/astrald/service/test/Message": {
        "type": "object",
        "properties": {
          "From": {
            "$ref": "github.com/cryptopunkscc/astrald/service/test/ContactId"
          },
          "Id": {
            "$ref": "github.com/cryptopunkscc/astrald/service/test/MessageId",
            "$schema": "http://json-schema.org/draft-04/schema"
          },
          "Text": {
            "type": "string"
          },
          "Time": {
            "type": "integer"
          },
          "To": {
            "$ref": "github.com/cryptopunkscc/astrald/service/test/ContactId"
          }
        },
        "additionalProperties": false
      },
      "github.com/cryptopunkscc/astrald/service/test/MessageId": {
        "type": "object",
        "properties": {
          "Value": {
            "type": "string"
          }
        },
        "additionalProperties": false
      },
      "github.com/cryptopunkscc/astrald/service/test/OverviewItem": {
        "type": "object",
        "properties": {
          "Contact": {
            "$ref": "github.com/cryptopunkscc/astrald/service/test/Contact",
            "$schema": "http://json-schema.org/draft-04/schema"
          },
          "LastMessage": {
            "$ref": "github.com/cryptopunkscc/astrald/service/test/Message",
            "$schema": "http://json-schema.org/draft-04/schema"
          }
        },
        "additionalProperties": false
      }
    }
  }
}
""".trimIndent()

private const val ref = "\$ref"
private const val schema = "\$schema"
