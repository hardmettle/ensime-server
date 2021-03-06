// Copyright: 2010 - 2017 https://github.com/ensime/ensime-server/graphs
// License: http://www.gnu.org/licenses/gpl-3.0.en.html
package org.ensime.fixture

import org.ensime.api._
import org.ensime.vfs._
import org.ensime.indexer._

trait SourceResolverFixture {
  def withSourceResolver(testCode: SourceResolver => Any): Any
  def withSourceResolver(testCode: (EnsimeConfig, SourceResolver) => Any): Any
}

trait IsolatedSourceResolverFixture
    extends SourceResolverFixture
    with IsolatedEnsimeConfigFixture
    with IsolatedTestKitFixture {
  override def withSourceResolver(testCode: SourceResolver => Any): Any = withEnsimeConfig { config =>
    withTestKit { testKit ⇒
      import testKit.system
      implicit val vfs = EnsimeVFS()
      try {
        testCode(new SourceResolver(config))
      } finally {
        vfs.close()
      }
    }
  }
  override def withSourceResolver(testCode: (EnsimeConfig, SourceResolver) => Any): Any = withEnsimeConfig { config =>
    withTestKit { testKit ⇒
      import testKit.system
      implicit val vfs = EnsimeVFS()
      try {
        testCode(config, new SourceResolver(config))
      } finally {
        vfs.close()
      }
    }
  }
}

trait SharedSourceResolverFixture extends SourceResolverFixture
    with SharedEnsimeConfigFixture with SharedTestKitFixture {
  this: SharedEnsimeVFSFixture =>

  private[fixture] var _resolver: SourceResolver = _
  override def beforeAll(): Unit = {
    super.beforeAll()
    implicit val system = _testkit.system
    _resolver = new SourceResolver(_config)
  }

  override def withSourceResolver(testCode: SourceResolver => Any): Any = testCode(_resolver)
  override def withSourceResolver(testCode: (EnsimeConfig, SourceResolver) => Any): Any = {
    testCode(_config, _resolver)
  }

}
