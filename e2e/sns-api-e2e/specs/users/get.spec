# ユーザーの一覧を取得する - users/get
tags: sequential
|ID|NAME|MAIL|
|--|--|--|
|7895cf5a-9986-454a-942a-e1c0dc16f814|田中太郎|taro@gmail.com|
|7895cf5a-9986-454a-942a-e1c0dc16f818|山田次郎|jiro@gmail.com|

* ユーザーの一覧を取得する

## ユーザーのIDを取得する
* JSONのキー"users"のある要素のキー"id"にバリュー<ID>が含まれる

## ユーザーの名前を取得する
* JSONのキー"users"の配列において、"id"が<ID>の要素の"name"のバリューが<NAME>である

## ユーザーのメールアドレスを取得する
* JSONのキー"users"の配列において、"id"が<ID>の要素の"mail"のバリューが<MAIL>である
