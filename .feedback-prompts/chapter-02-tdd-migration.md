目的:
chapter-02-fundamentals の exercises を TDD形式に移行する。
runMain での目視ではなく、最初から落ちるテストを通す形式にする。

対象:
- chapter-02-fundamentals/src/main/scala/exercises/*.scala
- chapter-02-fundamentals/src/test/scala
- chapter-02-fundamentals/README.md
- chapter-02-fundamentals/EXERCISES.md

要件:
1) 各 Exercise に対応する ScalaTest を追加
   - Exercise01Spec, Exercise02Spec, Exercise03Spec, Exercise04Spec
   - package は fundamentals.exercises
   - AnyFunSuite + Matchers など既存依存に合う軽量スタイルでOK
   - すべてのテストが「最初は落ちるが、実装後に通る」ようにする
2) Exercise02 は case class がコメントアウトされているため、
   テストがコンパイルできるように定義は有効化する
   （eval/exprToString は ??? のままでOK。テストは実行時に落ちる）
3) README/EXERCISES をTDD導線に変更
   - 「runMainで確認」ではなく「sbt test が主ルート」
   - runMain は任意の補助として残す
   - 最初に落ちるテストの場所と合格基準を明記
4) Exercise04 の flattenAndSum はヘッダの期待値(21)と実例(45)が矛盾。
   どちらかに統一して修正（テストと説明が一致するようにする）

テスト内容の具体例:
- Exercise01: factorial(5)=120, factorial(10)=3628800, applyNTimes(double,3,1)=8, applyNTimes(double,8,1)=256
- Exercise02: exprToString で "(2 + 3)" のような表記、eval の結果が 20/5/9 になること
- Exercise03: safeDivide / divideEven / parseAndDivide / getUserEmail の期待値
- Exercise04: processStrings, wordFrequency, flattenAndSum, cartesianProduct の期待値

注意:
- 既存の solutions はそのまま。テストが solutions で通ることは確認する想定
- 既存ファイルのコメントは必要なら軽く調整してよいが、演習意図は保持する
- 追加/変更したファイル一覧と、各Specで何をテストしているかの概要を最後にまとめる
