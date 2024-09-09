package com.example.simplememo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Controller
@RequestMapping("/")
public class NoteController {

    // データベース操作を簡単に行うためのJdbcTemplateを使うための変数
    private final JdbcTemplate jdbcTemplate;

    // コンストラクタでJdbcTemplateを注入する
    @Autowired
    public NoteController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        
        // noteという名前のテーブルがまだ存在していない場合は作成する
        jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS note (\n" +
                "  id INT AUTO_INCREMENT PRIMARY KEY,\n" +
                "  title TEXT,\n" +
                "  body TEXT,\n" +
                "  date TIMESTAMP\n" +
                ")"
        );
    }

    // トップページを表示するメソッド。ここでノートの一覧を取得して画面に渡す
    @GetMapping("/Simplememo/")
    public String index(ModelMap modelMap) {
        List<Note> noteList = new ArrayList<>(); // ノートを格納するリストを初期化
        // データベースから全てのノートを取得し、新しい順に並べ替える
        List<Map<String, Object>> dataList = jdbcTemplate.queryForList("SELECT * FROM note ORDER BY date DESC");
        
        // 取得したデータをNoteオブジェクトに変換し、リストに追加
        for (Map<String, Object> data : dataList) {
            noteList.add(mapToNote(data));
        }
        
        // モデルにnoteListを追加し、テンプレートエンジンに渡す
        modelMap.addAttribute("noteList", noteList);
        return "index"; // index.htmlというテンプレートを表示
    }

    // 新規ノートを作成するための画面を表示する
    @GetMapping("/Simplememo/add")
    public String addGet() {
        return "add"; // add.htmlというテンプレートを表示
    }

    // 新規ノートを保存する処理
    @PostMapping("/Simplememo/add")
    public String addPost(@RequestParam("title") String title, @RequestParam("body") String body) {
        // 送信されたタイトルと本文をデータベースに保存する
        jdbcTemplate.update("INSERT INTO note (title, body, date) VALUES (?, ?, ?)", title, body, new Date());
        return "redirect:/Simplememo/"; // 保存後、トップページにリダイレクト
    }

    // ノートを編集するための画面を表示する
    @GetMapping("/Simplememo/edit")
    public String editGet(@RequestParam("id") int id, ModelMap modelMap) {
        // 指定されたIDのノートを取得する
        Note note = getNoteById(id);
        // 取得したノートをモデルに追加し、テンプレートに渡す
        modelMap.addAttribute("note", note);
        return "edit"; // edit.htmlというテンプレートを表示
    }

    // ノートの編集を保存する処理
    @PostMapping("/Simplememo/edit")
    public String editPost(@RequestParam("id") int id, @RequestParam("title") String title, @RequestParam("body") String body) {
        // 送信された内容で指定されたIDのノートを更新する
        jdbcTemplate.update("UPDATE note SET title = ?, body = ?, date = ? WHERE id = ?", title, body, new Date(), id);
        return "redirect:/Simplememo/"; // 更新後、トップページにリダイレクト
    }

    // ノートを削除する処理
    @GetMapping("/Simplememo/delete")
    public String deleteGet(@RequestParam("id") int id) {
        // 指定されたIDのノートをデータベースから削除する
        jdbcTemplate.update("DELETE FROM note WHERE id = ?", id);
        return "redirect:/Simplememo/"; // 削除後、トップページにリダイレクト
    }

    // ノートの詳細を表示する画面
    @GetMapping("/Simplememo/view/{id}")
    public String viewGet(@PathVariable("id") int id, ModelMap modelMap) {
        // 指定されたIDのノートを取得する
        Note note = getNoteById(id);
        // 取得したノートをモデルに追加し、テンプレートに渡す
        modelMap.addAttribute("note", note);
        return "view"; // view.htmlというテンプレートを表示
    }

    // 指定されたIDのノートを取得するヘルパーメソッド
    private Note getNoteById(int id) {
        // データベースからIDに一致するノートを取得する
        List<Map<String, Object>> dataList = jdbcTemplate.queryForList("SELECT * FROM note WHERE id = ?", id);
        if (dataList.isEmpty()) {
            // 指定したIDのノートが存在しない場合はエラーをスロー
            throw new IllegalArgumentException("Note with id " + id + " not found.");
        }
        // 取得したデータをNoteオブジェクトに変換して返す
        return mapToNote(dataList.get(0));
    }

    // データベースから取得したデータをNoteオブジェクトに変換するヘルパーメソッド
    private Note mapToNote(Map<String, Object> data) {
        Note note = new Note();
        note.id = (int) data.get("id"); // IDを取得
        note.title = (String) data.get("title"); // タイトルを取得
        note.body = (String) data.get("body"); // 本文を取得
        note.date = (Date) data.get("date"); // 日付を取得
        return note; // Noteオブジェクトを返す
    }
}
