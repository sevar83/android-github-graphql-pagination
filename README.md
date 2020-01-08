# android-github-graphql-pagination

Android experiment with AndroidX Paging + Apollo GraphQL client + GitHub GraphQL API. 

Implements `PageKeyedDataSource` with 2 different queries for forward and backward directions. The initial page is always the first one. Not yet sure if AndroidX Paging supports an arbitrary initial page.

TODO:
- Error handling, retries
- Placeholders
- Repository detail screen
- Commits screen
