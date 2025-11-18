# Fix: FXML Resources Not Being Copied

## The Problem

Error: `FXML file not found: /SceneBuilder/login uis/LogIn.fxml`

The resources are in `src/resources/` but Maven expects them in `src/main/resources/` by default.

## Solution Applied

Updated `pom.xml` to include both resource directories:

```xml
<resources>
    <resource>
        <directory>src/resources</directory>
    </resource>
    <resource>
        <directory>src/main/resources</directory>
    </resource>
</resources>
```

## Next Steps

1. **Clean and rebuild**:
   ```bash
   mvn clean compile
   ```

2. **Verify resources are copied**:
   Check that this file exists:
   ```
   target/classes/SceneBuilder/login uis/LogIn.fxml
   ```

3. **Run the application**:
   ```bash
   mvn javafx:run
   ```

## Alternative Solution

If you prefer to use standard Maven structure, move resources:

```bash
# Move resources to standard location
mv src/resources src/main/resources
```

Then remove the custom `<resources>` configuration from `pom.xml`.

## Why This Happens

Maven's default resource directory is `src/main/resources/`. If you use a different location like `src/resources/`, you need to configure it in `pom.xml`.

