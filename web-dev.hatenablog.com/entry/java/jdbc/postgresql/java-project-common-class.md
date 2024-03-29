---
Title: JDBC：Java共通資源の作成
Category:
- Java
Date: 2017-11-03T10:45:15+09:00
URL: http://web-dev.hatenablog.com/entry/java/jdbc/postgresql/java-project-common-class
EditURL: https://blog.hatena.ne.jp/mamorums/web-dev.hatenablog.com/atom/entry/8599973812313993351
---

JDBC を使うための Java のプロジェクトや、PostgreSQL（RDBMS）に接続する共通的なクラスを作成していきます。


## 手順1. プロジェクトの作成
`jdbc-pg` というディレクトリを作成して、その配下に `src/main/java` というディレクトリ階層を作成します。

```
jdbc-pg/
  - pom.xml
  - src/
    - main/
      - java/
```

`src/main/java` には、ソースコードを置いていきます。


## 手順2. pom.xml の作成
Maven でビルドできるように、以下の `pom.xml` を作成します。

`jdbc-pg/pom.xml`

```
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <groupId>com.github.mamorum</groupId>
  <artifactId>jdbc-pg</artifactId>
  <version>1.0.0</version>
  <packaging>jar</packaging>

  <dependencies>
    <dependency>
      <groupId>org.postgresql</groupId>
      <artifactId>postgresql</artifactId>
      <version>42.1.4</version>
    </dependency>
  </dependencies>

  <properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
    <maven.compiler.source>1.8</maven.compiler.source>
    <maven.compiler.target>1.8</maven.compiler.target>
  </properties>
</project>
```

PostgreSQL の JDBC ドライバを使うので、`dependencies` タグを使って依存するようにしています。


## 手順3. DB接続クラスの作成
DBに接続するための共通クラスを作成します。メソッドを１つ用意して、`java.sql.Connection` を返すようにしています。

`jdbc-pg/src/main/java/basic/Pg.java`

```java
package basic;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Pg {
  public static Connection connect()
    throws ClassNotFoundException, SQLException
  {
    Class.forName("org.postgresql.Driver");
    return DriverManager.getConnection(
      "jdbc:postgresql://localhost/test",  // url
      "neko", "cat" // user, password
    );
  }
}
```

SQL（select, update, etc）を実行するときに `Connection` が必要になるので共通化させています。

接続情報は以下の通りになります。

- データベース名: `test`
- ログインユーザ: `neko`
- パスワード:  `cat`

文字列 `jdbc:postgresql....` で、ローカルの `test` データベースに接続するようにしています。内容は、[PostgreSQL のマニュアル](https://jdbc.postgresql.org/documentation/head/connect.html) に従っています。


## 手順4. 動作確認
### 手順4.1. 動作確認クラスの作成
`Connection` を取得して、簡単な SQL を実行するクラスを作成します。

`jdbc-pg/src/main/java/basic/ConnectMain.java`

```java
package basic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ConnectMain {
  public static void main(String[] args)
    throws ClassNotFoundException, SQLException
  {
    try (Connection con = Pg.connect()) {
      //-> 現在日時を取得するSQLを準備
      PreparedStatement ps = con.prepareStatement(
        "select current_timestamp"
      );
      //-> SQLを実行してデータを取得
      ResultSet rs = ps.executeQuery();
      //-> データのカーソルを１つ進める
      rs.next();
      //-> データを表示
      System.out.print("Now ");
      System.out.println(rs.getTimestamp("now"));
      //-> 後処理
      rs.close();
      ps.close();
    }
  }
}
```

try節（`try (Cooection con = ....`）は、 `Connection` を使い終わったらクローズするために書いています。try-with-resources文というみたいで、詳細は以下のリンク先に書かれています。

[try-with-resources 文 - Oracle](https://docs.oracle.com/javase/jp/7/technotes/guides/language/try-with-resources.html)

処理内容は、DB の現在日時（タイムスタンプ）を select して表示しています。SQL を実行すると、PostgreSQL が `now` という列を返すので、それを `rs.getTimestamp("now")` で取得しています。


### 手順4.2. 動作確認クラスの実行
事前に PostgreSQL を起動して、データベースとユーザを作成しておきます。作成方法は記事「[JDBC：DB環境の準備](/entry/java/jdbc/postgresql/db-env)」に書いていますので、必要に応じて参照して頂けると嬉しいです。 

それから上の `ConnectMain` クラスを実行すると、以下のように表示されます。

```
Now 2017-11-03 10:28:14.25525
```
