# Fix: IllegalStateException - Location is not set

## The Problem

Error:
```
Caused by: java.lang.IllegalStateException: Location is not set.
        at javafx.fxml.FXMLLoader.loadImpl(FXMLLoader.java:2556)
        at Main.start(Main.java:33)
```

## Root Cause

The `Main` class is in the **default package** (no package declaration), and `getClass().getResource()` might not work correctly. The FXML file path cannot be resolved.

## Solution Applied

Changed from:
```java
FXMLLoader loader = new FXMLLoader(getClass().getResource("/SceneBuilder/login uis/LogIn.fxml"));
```

To:
```java
java.net.URL fxmlUrl = Main.class.getResource("/SceneBuilder/login uis/LogIn.fxml");
if (fxmlUrl == null) {
    throw new IOException("FXML file not found: /SceneBuilder/login uis/LogIn.fxml");
}
FXMLLoader loader = new FXMLLoader(fxmlUrl);
```

## Additional Steps

### 1. Clean and Rebuild

```bash
mvn clean compile
```

This ensures resources are copied to `target/classes/`.

### 2. Verify Resources Directory

Make sure the FXML file exists at:
```
src/resources/SceneBuilder/login uis/LogIn.fxml
```

### 3. Check Resources Are Copied

After compiling, verify:
```
target/classes/SceneBuilder/login uis/LogIn.fxml
```

If this file doesn't exist, Maven isn't copying resources. Check `pom.xml` for resources configuration.

## Alternative Solution

If the above doesn't work, try using ClassLoader:

```java
java.net.URL fxmlUrl = Main.class.getClassLoader().getResource("SceneBuilder/login uis/LogIn.fxml");
FXMLLoader loader = new FXMLLoader(fxmlUrl);
```

Note: Remove the leading `/` when using ClassLoader.

## Verify Fix

After making changes:
1. Run `mvn clean compile`
2. Run `mvn javafx:run`
3. Application should start without "Location is not set" error

