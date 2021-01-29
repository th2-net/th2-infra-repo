# th2-infra-repo
Java library for working with schema repositories.
General skeleton for working with repository looks like this:

      Gitter gitter;
      // retrieve instance
      gitter.lock()
      try {
          // do operations on repository
      } finally {
          gitter.unlock();
      }


There are two main classes to work with:

      Gitter - To work with remote git repository
      Repository  - To work with local cache of the repository