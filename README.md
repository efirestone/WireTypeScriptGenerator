# TypeScript Generator For Wire

This project provides an implementation of [Wire's](https://github.com/square/wire) CustomHandler interface which
generates TypeScript type definitions for Protobuf messages and enums.

The generated types are designed for use with [class-transformer](https://github.com/typestack/class-transformer), and
include the necessary `@Type` decorators. They are therefore meant for use with JSON requests and responses and not
protobuf bytes. Wire's [JSON transformation](https://square.github.io/wire/wire_json/) functionality allows you to build
protobuf messages in your server code, then encode them as JSON to send to your website.

## Using The Generator With Gradle

Include the plugin, and the Wire plugin, in your `buildscript` block. If you have Gradle subprojects configured, this
block will usually live in your top-level `build.gradle[.kts]`.

```kotlin
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("com.squareup.wire:wire-gradle-plugin:$wire_version")
        classpath("com.codellyrandom:wire-typescript-generator:$wire_typescript_version")
    }
}
```

In the `build.gradle[.kts]` file for the project (or appropriate subproject), include the Wire plugin:

```kotlin
plugins {
    id("com.squareup.wire")
    // ...
}
```

Finally, configure the `wire` block to specify that we're using the TypeScript generator:

```kotlin
wire {
    custom {
        customHandlerClass = "com.codellyrandom.wiretypescriptgenerator.TypeScriptGenerator"

        exclusive = true
        out = "${buildDir}/custom"
    }
}
```

If you're using Gradle subprojects and sharing the protos between multiple of those subprojects (say your front end web
and your server), you can create one subproject that just contains the protos, and then reference that project from the
other two:

The `build.gradle[.kts]` for the subproject containing the protos should look something like:

```kotlin
plugins {
  kotlin("jvm")
  application
  id("com.squareup.wire")
}

wire {
  // This makes the protos available to other subprojects
  protoLibrary = true
}
```

In your web subproject that will generate TypeScript types:
```kotlin
wire {
    sourcePath {
        // "api" is the name of the subproject that contains your protos.
        srcProject(":api")
    }
    custom {
        schemaHandlerFactoryClass = "com.codellyrandom.wiretypescriptgenerator.TypeScriptGeneratorFactory"

        // This is optional, but lets you limit which protos
        // get translated into TypeScript.
        includes = listOf(
            "my.project.media.*",
            "my.project.settings.*",
        )
        exclusive = true
        out = "${buildDir}/custom"
    }
}
```

### Importing The Generated Files

The generated `.ts` files will end up wherever the `out` setting in the `wire` block of the Gradle config directs them.
This can either be somewhere along with your other code, and are then usually checked in. Alternatively, this can be
somewhere in your build folder, in which case the files are usually not checked in (recommended).

You can make importing these files into your TypeScript easier by using aliases.

In your tsconfig.json:

```json
{
  "compilerOptions": {
    ...
    "baseUrl": ".",
    "paths": {
      "@/*": [
        "src/*"
      ],
      // This allows this folder to be imported as "@api/"
      "@api/*": [
        "build/custom/my/project/*"
      ]
    },
    ...
```

Additionally, you'll likely need to configure aliases in your compiler. For [Babel](https://babeljs.io), you'll want to
install the [babel-plugin-module-resolver](https://github.com/tleunen/babel-plugin-module-resolver) and configure your
`.babelrc` like so:

```json
{
  "presets": ["vue"], // Find and install the presets for your framework
  "plugins": [
    [
      "module-resolver",
      {
        "alias": {
          "@api": "./build/custom/my/project"
        }
      }
    ]
  ]
}
```

With these in place, you can now import something like `./bulid/custom/my/project/Foo.ts` like so:

```typescript
import Foo from "@api/Foo"
```

## Generated Code

The generator turns protobuf messages into TypeScript classes, and protobuf enums into TypeScript enums.

### Protobuf Messages

Protobuf messages are generated as TypeScript classes. Some details:
- The message's fields become instance variables
- Optional fields are defined as `<type> | undefined` and are assigned a default of `undefined`
- Required fields are not assigned a default and are defined as parameters in the constructor, thus requiring that an
  instance cannot exist without the field having a value.
- Protobuf `oneof`s are generated as flat fields and it's up to the caller to ensure that only one of the fields is
  populated.

#### Nested Messages

TypeScript does not support nested classes, so nested messages must be represented as top-level classes. Nested messages
are named such that they include their parent types for uniqueness.

```protobuf
package my.project;

message Foo {
  message Bar {
    message Baz {}
  }
}
```

This will generate three TypeScript classes:

```typescript
default export class Foo {}

export class Foo_Bar {}

export class Foo_Bar_Baz {}
```

Each top-level message in your protobuf files will get its own `.ts` file, and the class for that top-level message will
be the default export. Any nested messages within that top-level message will be generated in the same file and will be
regular exports.

To import just the default type in your TypeScript:

```typescript
import Foo from "@/my/project/Foo"
```

To import the default type as well as subtypes:
```typescript
import Foo, {
  Foo_Bar as Bar,
  Foo_Bar_Baz as Baz,
} from "@/my/project/Foo"
```

### Protobuf Enums

Protobuf enums are generated as TypeScript enums with string values.

```protobuf
enum MyNumber {
  ZERO = 0;
  ONE = 1;
  TWO = 2;
}
```

becomes

```typescript
enum MyNumber {
  ZERO = "ZERO",
  ONE = "ONE",
  TWO = "TWO",
}
```

Top-level enums in your protobufs will get their own `.ts` file, and nested enums will be generated within the file for
the top-level type in which they are contained.

### Protobuf RPC Services

Services are supported and generate a TypeScript class of the same name.

This protobuf service:

```protobuf
service DinosaursService {
  rpc Stampede(StampedeRequest) returns (StampedeResponse);
}
```

becomes:

```typescript
export default class DinosaursService {
  client: ServiceNetworkClient
  
  constructor(client: ServiceNetworkClient) {
    this.client = client
  }

  async stampede(request: StampedeRequest): Promise<StampedeResponse> {
    const response = await this.client.post("dinosaurs/stampede", serialize(request))
    return plainToClass(StampedeResponse, response.data as JSON)
  }
}
```

The service class (`DinosaursService` in this example) takes as an argument a `ServiceNetworkClient`, which is
responsible for making the actual network requests. This is designed to be easily compatible with [Axios](https://github.com/axios/axios),
and an `AxiosInstance` can be passed in as the `ServiceNetworkClient`. Other network clients will work as well, but
might need a small wrapper in order to conform to `ServiceNetworkClient`.

Each `rpc` definition in the protobuf service will be generated as a method in the service class. The URL path for each
endpoint is derived from the service's name and the RPC's name: `<Service Name>/<RPC Name>`. Both are converted to kebab
case, and the word "Service" is removed if it's present as suffix on the service class name.

It's likely that you'll want to wrap the service within another controller, like so:

```typescript
export class NetworkDinosaursStore {
  service = new DinosaursService(axios as ServiceNetworkClient)

  async stampede(count: number) {
    const request = new StampedeRequest(count)

    // Response is a StampedResponse
    const response = await this.service.stampede(request)
    // ...do something with the response...
  }
}
```

### Packages

The Protobuf package is used for the path of a given generated type, but is not encoded into the type itself in any way.
This means that if both `my.project.settings.Foo` and `my.project.media.Foo` exist then both will be
created as TypeScript classes named `Foo`, but at different paths. When using these types in your code this generally
won't matter because you'll only import one of the two. If you do need to import both in the same file you can use
import aliases to differentiated them:

```typescript
import Foo as MediaFoo from "@/my/project/media/Foo"
import Foo as SettingsFoo from "@/my/project/settings/Foo"
```
