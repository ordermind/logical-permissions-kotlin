<a href="https://travis-ci.org/ordermind/logical-permissions-kotlin" target="_blank"><img src="https://travis-ci.org/ordermind/logical-permissions-kotlin.svg?branch=master" /></a>
# logical-permissions

This is a generic library that provides support for json-based permissions with logic gates such as AND and OR. You can register any kind of permission types such as roles and flags. The idea with this library is to be an ultra-flexible foundation that can be used by any framework. It is written for JVM 1.8.

## Getting started

### Installation

@TODO Installation

### Usage

```kotlin
// Simple example for checking user roles

// @TODO Import package

fun main(args : Array<String>) {
    val lp = LogicalPermissions()
    
    // Add callback function for permission type "role"
    val roleCallback = fun(role: String, context: Map<String, Any>): Boolean {
        if (!context.containsKey("user")) return false
    
        val user = context["user"]
        if (user == null) return false
        if (user !is Map<*, *>) return false
    
        val roles = user["roles"]
        if (roles == null) return false
        if (roles !is Set<*>) return false
    
        return roles.contains(role)
    }
    
    // Add permission type "role"
    lp.addType("role", roleCallback)
    
    // Create sample user that has a "writer" role
    val user = mapOf("id" to 1, "roles" to setOf("writer"))
    
    // Create sample permissions that allows access if the user is either an editor or a writer
    val permissions =
    """
    {
        "role": ["editor", "writer"]
    }
    """
    
    // Check access
    val access = lp.checkAccess(permissions, mapOf("user" to user))
    println("Access granted: $access")
}

```

The main api method is [`LogicalPermissions::CheckAccess()`](/docs/logical-permissions/-logical-permissions-interface/check-access.md), which checks the access for a **permission tree**. A permission tree is a bundle of permissions that apply to a specific action. Let's say for example that you want to restrict access for updating a user. You'd like only users with the role "admin" to be able to update any user, but users should also be able to update their own user data (or at least some of it). With the structure this package provides, these conditions could be expressed elegantly in a permission tree as such:

```kotlin
val permissions = 
"""
{
  "OR": {
    "role": "admin",
    "flag": "is_author"
  }
}
"""
```

In this example `role` and `flag` are the evaluated permission types. For this example to work you will need to register the permission types "role" and "flag" so that the LogicalPermissions object knows which callbacks are responsible for evaluating the respective permission types. You can do that with [`LogicalPermissions::AddType()`](/docs/logical-permissions/-logical-permissions-interface/add-type.md).

### Bypassing permissions
This packages also supports rules for bypassing permissions completely for superusers. In order to use this functionality you need to register a callback by setting the [`bypassCallback`](/docs/logical-permissions/-logical-permissions-interface/bypass-callback.md) property. The registered callback will run on every permission check and if it returns `true`, access will automatically be granted. If you want to make exceptions you can do so by adding `"NO_BYPASS": true` to the first level of a permission tree. You can even use permissions as conditions for `NO_BYPASS`.

Examples:

```kotlin
// Disallow access bypassing
val permissions =
"""
{
  "NO_BYPASS": true,
  "role": "editor"
}
"""
```

```kotlin
// Disallow access bypassing only if the user is an admin
val permissions =
"""
{
  "NO_BYPASS": {
    "role": "admin"
  },
  "role": "editor"
}
"""
```

## Logic gates

Currently supported logic gates are [AND](#and), [NAND](#nand), [OR](#or), [NOR](#nor), [XOR](#xor) and [NOT](#not). You can put logic gates anywhere in a permission tree and nest them to your heart's content. All logic gates support a json object or array as their value, except the NOT gate which has special rules. If a value that is a json object or array of values does not have a logic gate as its key, an OR gate will be assumed.

### AND

A logic AND gate returns true if all of its children return true. Otherwise it returns false.

Examples:

```kotlin
// Allow access only if the user is both an editor and a sales person
val permissions =
"""
{
  "role": {
    "AND": ["editor", "sales"]
  }
}
"""
```

```kotlin
// Allow access if the user is both a sales person and the author of the document
val permissions =
"""
{
  "AND": {
    "role": "sales",
    "flag": "is_author"
  }
{
"""
```

### NAND

A logic NAND gate returns true if one or more of its children returns false. Otherwise it returns false.

Examples:

```kotlin
// Allow access by anyone except if the user is both an editor and a sales person
val permissions =
"""
{
  "role": {
    "NAND": ["editor", "sales"]
  }
{
"""
```

```kotlin
// Allow access by anyone, but not if the user is both a sales person and the author of the document.
val permissions =
"""
{
  "NAND": {
    "role": "sales",
    "flag": "is_author"
  }
{
"""
```

### OR

A logic OR gate returns true if one or more of its children returns true. Otherwise it returns false.

Examples:

```kotlin
// Allow access if the user is either an editor or a sales person, or both.
val permissions =
"""
{
  "role": {
    "OR": ["editor", "sales"]
  }
{
"""
```

```kotlin
// Allow access if the user is either a sales person or the author of the document, or both
val permissions =
"""
{
  "OR": {
    "role": "sales",
    "flag": "is_author"
  }
{
"""
```

### Shorthand OR

As previously mentioned, any value that is a json object or array and doesn't have a logic gate as its key is interpreted as belonging to an OR gate.

In other words, this permission tree:

```kotlin
val permissions =
"""
{
  "role": ["editor", "sales"]
{
"""
```
is interpreted exactly the same way as this permission tree:
```kotlin
val permissions =
"""
{
  "role": {
    "OR": ["editor", "sales"]
  }
{
"""
```

### NOR

A logic NOR gate returns true if all of its children returns false. Otherwise it returns false.

Examples:

```kotlin
// Allow access if the user is neither an editor nor a sales person
val permissions =
"""
{
  "role": {
    "NOR": ["editor", "sales"]
  }
{
"""
```

```kotlin
// Allow neither sales people nor the author of the document to access it
val permissions =
"""
{
  "NOR": {
    "role": "sales",
    "flag": "is_author"
  }
{
"""
```


### XOR

A logic XOR gate returns true if one or more of its children returns true and one or more of its children returns false. Otherwise it returns false. An XOR gate requires a minimum of two elements as its value.

Examples:

```kotlin
// Allow access if the user is either an editor or a sales person, but not both
val permissions =
"""
{
  "role": {
    "XOR": ["editor", "sales"]
  }
{
"""
```

```kotlin
// Allow either sales people or the author of the document to access it, but not if the user is both a sales person and the author
val permissions =
"""
{
  "XOR": {
    "role": "sales",
    "flag": "is_author"
  }
{
"""
```

### NOT

A logic NOT gate returns true if its child returns false, and vice versa. The NOT gate is special in that it supports either a String or a json object with a single element as its value.

Examples:

```kotlin
// Allow access for anyone except editors
val permissions =
"""
{
  "role": {
    "NOT": "editor"
  }
{
"""
```

```kotlin
// Allow access for anyone except the author of the document
val permissions =
"""
{
  "NOT": {
    "flag": "is_author"
  }
{
"""
```

## Boolean Permissions

Boolean permissions are a special kind of permission. They can be used for allowing or disallowing access for everyone (except those with bypass access). They are not allowed as descendants to a permission type and they may not contain children. Both real booleans and booleans represented as uppercase strings are supported. Of course a simpler way to allow access to everyone is to not define any permissions at all for that action, but it might be nice sometimes to explicitly allow access for everyone.

Examples:

```kotlin
// Allow access for anyone
val permissions =
"""
[
  true
]
"""

//Using a boolean without an array is also permitted
val permissions = true
```

```kotlin
// Example with String representation
val permissions =
"""
[
  "TRUE"
]
"""

// Using a String representation without an array is also permitted
val permissions = "TRUE"
```

```kotlin
//Deny access for everyone except those with bypass access
val permissions =
"""
[
  false
]
"""

//Using a boolean without an array is also permitted
val permissions = false
```

```kotlin
//Example with String representation
val permissions =
"""
[
  "FALSE"
]
"""

//Using a String representation without an array is also permitted
val permissions = "FALSE"
```

```kotlin
//Deny access for everyone including those with bypass access
val permissions =
"""
{
  "0": false,
  "NO_BYPASS": true
{
"""
```

## API Documentation
Please refer to [`LogicalPermissions::AddType()`](/docs/logical-permissions/-logical-permissions-interface/index.md) for api documentation.