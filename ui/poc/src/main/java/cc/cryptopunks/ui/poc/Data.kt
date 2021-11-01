package cc.cryptopunks.ui.poc

val sampleLayout = """
{
  "type": "LinearLayout",
  "orientation": "vertical",
  "padding": "16dp",
  "children": [{
    "type": "TextView",
    "layout_width": "200dp",
    "gravity": "center",
    "text": "@{user.profile.name}"
  }, {
    "type": "HorizontalProgressBar",
    "layout_width": "200dp",
    "layout_marginTop": "8dp",
    "max": 6000,
    "progress": "@{user.profile.experience}"
  }]
}
""".trimIndent()

val sampleData = """
{
  "user": {
    "profile": {
      "name": "John Doe",
      "experience": 4192
    }
  }
}
""".trimIndent()
