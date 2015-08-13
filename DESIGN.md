# Design Notes

## Goals

- reusable library
- small API surface
- usage agnostic
- allowing configuration changes in an online (against running server)
  or offline (against a configuration file of a stopped server) fashion
- provide a helping hand with testing management operations

## Non-goals

- dragging a lot of dependencies to user's classpath
- command-line application for applying configuration changes (is a worthwhile
  supplementary project though)
- integration with other projects (worthwhile supplementary project too)
- testing management operations

## Operation vs. Command

- operation is what you can pass to `ModelControllerClient.execute()`; i.e.,
  a low-level unit of management API
- command is the single abstraction this library brings; a higher-level set
  of operations that provide a well-defined management functionality
- commands can be implemented both in terms of operations and in terms of
  other commands

## Error Handling

- operations are just performed and no error checking is done; errors might
  even be intentional (issuing a malicious operation and checking for error
  in a test)
- commands are supposed to always succeed; all operations will be checked
  and if an error is detected, the entire execution is abruptly terminated and
  no guarantees are made

## Management Client

- an entrypoint that can perform operations and commands
- online management and offline management are separate and have separate
  entrypoints (`OnlineManagementClient` and `OfflineManagementClient`)
- online and offline commands are also separate (`OnlineCommand` and
  `OfflineCommand`), which provides for compile-time type checking

## Concurrency

- no need to care about concurrency for operations (they are atomic),
  only for commands
- simplest solution: running commands is only allowed when no other management
  clients are present; this is arguably a vast majority of all use cases,
  but it can't be enforced
- management API supports composite operations (batches), which is the closest
  thing to transactions available, but they are not sufficient for all of our
  use cases for commands

## Different Management Versions

- not handled for operations
- commands get a `ManagementVersion` object corrensponding to current version
  and can decide on that
- that implies that right after creating a `ManagementClient`, its management
  version must be detected (both in online and offline case)

## Testing

- core API needs unit testing especially in the error handling area
- the entire library needs integration testing in both online and offline mode
  (check that operations are correctly applied)
- the purpose of the tests here is _not_ to test the management operations
  themselves
