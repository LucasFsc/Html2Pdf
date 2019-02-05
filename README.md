# Html2Pdf
Android library to convert Html from strings into Pdf files.

## Download

```gradle
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}

implementation 'com.github.LucasFsc:TitleNavigation:0.1-alpha'

```

## How do I use it?

In your MainActivity instantiate the converter class and pass the parameters:

```kotlin
class MainActivity : AppCompatActivity(), Html2Pdf.OnCompleteConversion /* Callback */ {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Impl example
        val converter = Html2Pdf.Companion.Builder()
            .context(this)
            .html("<p> Your awesome HTML string here! </p>")
            .file(File(URI("Your Pdf file URI")))
            .build()

        //can be called with a callback to warn the user, share file...
        converter.convertToPdf(this)

        //or without a callback
        converter.convertToPdf()
        
    }

    override fun onSuccess() {
        //do your thing
    }

    override fun onFailed() {
        //do your thing
    }
    
}
```

## License
[MIT](https://github.com/LucasFsc/Html2Pdf/blob/master/LICENSE)
