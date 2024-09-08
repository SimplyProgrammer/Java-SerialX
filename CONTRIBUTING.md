# Contributing to SerialX

First off, thank you for considering contributing to this project. It's people like you that make this project even better!

## How Can I Contribute?

### Reporting Bugs

- **Ensure the bug was not already reported** by searching on GitHub under [Issues section](https://github.com/PetoPetko/Java-SerialX/issues).
- If you're unable to find an open issue addressing the problem, [open a new one](https://github.com/PetoPetko/Java-SerialX/issues/new).
  - Make sure that the issue contains well enough description and if possible some additional data as well, such as code examples. This can help to better understand the issue or ease the reproduction process. 

### Your First Code Contribution

- Fork the repo.
- Windows only: Download [Git bash x64](https://git-scm.com/download/) if you do not have it already.
- Open bash/git bash then run the following command and write in your GitHub account name (SimplyProgrammer in my case). This command should properly clone your forked SerialX repo:
```
read -p "Enter your GitHub account name: " name && mkdir -p SerialX && cd "$_" &&
git clone --single-branch --branch dev "https://github.com/$name/Java-SerialX.git" SerialXDev &&
git clone --single-branch --branch tests-and-experimental-features "https://github.com/$name/Java-SerialX.git" SerialXTest
```
- Import both SerialXDev and SerialXTest together with all of their modules into your editor of choice, I recommend Eclipse.
- Make some changes (add something feature, fix some bugs, improve Javadocs...)
- Go to SerialXTest and run `examples.implementations.GeneralExample`, `examples.implementations.SimpleQuerying` and `examples.implementations.SerializingWithJson` junit tests. Acknowledge that only changes that meet all the tests can be added to the library!
- If all the tests are green you can `git add .`
- Commit your changes (`git commit -am 'describe what you have done (adding/fixing/etc something)'`).
- Push to the branch (`git push`).
- When you are done, open a new Pull Request.

## Styleguides and code requirements
- Follow general Java conventions.
- Introduce as few boundaries as possible, try making things as universal as possible (do not use final or private if possible).
- Try for your code to not stick out stylistically ;)
- Every added feature must have an outreaching purpose, must be tested, and perform reasonably.
  - Make sure to NOT alter any of the tests unless your use case explicitly requires it, in that case make sure to document it!  
- Every added class and method must be documented (/** doc */) and contain @author and @version. Method does not need to have @author, especially @author of the class is the same.
- Note: Version does not have to be incremented in any way, adding _SNAPSHOT at the end should be sufficient.

## Additional

Thank you for contributing to SerialX!
