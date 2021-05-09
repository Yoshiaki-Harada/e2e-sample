## TDDの練習を行う為のSNSのCloneアプリ

#### Databaseの準備 (Dockerでpostgresを起動)
1 environments/db/build.sh を実行する
2 environments/db/run.sh　を実行する

#### テーブル設計
![テーブル設計](/table.png) 

#### ドメインの説明
+ User: Messageやコメントを行う主体
+ Tweet: ツイートと同じ概念
    + replyToにTweetへの参照があるとコメントとなる 
+ Tag: MessageにはTagをつけることができる

#### 現状できること
+ Userの作成と更新と一覧
+ Messageの作成と更新と閲覧
+ Commentの作成と更新と閲覧
#### 設計の特徴
+ CQRSの考えでPortの層でReadとWriteは完全に別れている
    + Queryはレスポンスに必要な型を直接返している
+ トランザクションはRepositoryで貼っている
