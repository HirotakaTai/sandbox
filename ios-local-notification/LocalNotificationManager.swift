//
//  LocalNotificationManager.swift
//  ios-local-notification
//
//  Created by GitHub Copilot on 2026/05/04.
//

import Combine
import Foundation
import UserNotifications

/// ローカル通知に関する処理をまとめるクラスです。
///
/// SwiftUI の View に直接 `UNUserNotificationCenter` の処理を書くこともできますが、
/// 初学者向けサンプルでは「画面」と「通知APIの使い方」を分けたほうが読みやすくなります。
@MainActor
final class LocalNotificationManager: ObservableObject {
    @Published private(set) var authorizationStatusText = "未確認"
    @Published private(set) var pendingRequests: [PendingNotificationItem] = []
    @Published private(set) var latestMessage = "通知の許可状態を確認してからサンプルを予約します。"
    @Published private(set) var isWorking = false

    private let notificationCenter: UNUserNotificationCenter

    /// `UNUserNotificationCenter.delegate` は弱参照です。
    /// そのため、delegate オブジェクトをプロパティで保持しておく必要があります。
    private let foregroundDelegate = LocalNotificationForegroundDelegate()

    init(notificationCenter: UNUserNotificationCenter = .current()) {
        self.notificationCenter = notificationCenter
        self.notificationCenter.delegate = foregroundDelegate
    }

    /// 画面表示に必要な「権限」と「予約中の通知」をまとめて読み込みます。
    func refreshAll() async {
        guard !isWorking else { return }

        isWorking = true
        defer { isWorking = false }

        await updateAuthorizationStatus()
        await updatePendingRequests()
        latestMessage = "現在の状態を読み込みました。"
    }

    /// 通知許可をリクエストします。
    ///
    /// iOS の通知許可ダイアログは、基本的に初回リクエスト時にだけ表示されます。
    /// 2回目以降はシステム設定に保存された結果が返ってくる点が学習ポイントです。
    func requestAuthorization() async {
        guard !isWorking else { return }

        isWorking = true
        defer { isWorking = false }

        do {
            let granted = try await notificationCenter.requestAuthorization(options: [.alert, .sound])
            await updateAuthorizationStatus()

            latestMessage = granted
                ? "通知が許可されました。サンプル通知を予約できます。"
                : "通知は許可されませんでした。設定アプリで許可を変更できます。"
        } catch {
            latestMessage = "通知許可のリクエストに失敗しました: \(error.localizedDescription)"
        }
    }

    /// 選択されたパターンの通知を予約します。
    ///
    /// ここでは予約前に必ず `notificationSettings()` を読みます。
    /// ユーザーは設定アプリからいつでも通知許可を変更できるためです。
    func schedule(_ pattern: NotificationSamplePattern) async {
        guard !isWorking else { return }

        isWorking = true
        defer { isWorking = false }

        let authorizationStatus = await updateAuthorizationStatus()
        guard authorizationStatus.canScheduleNotifications else {
            latestMessage = authorizationStatus.messageForBlockedScheduling
            return
        }

        let request = makeRequest(for: pattern)

        // 同じサンプルを何度も押したときに予約が増え続けないよう、同じIDの予約を置き換えます。
        notificationCenter.removePendingNotificationRequests(withIdentifiers: [pattern.requestIdentifier])

        do {
            try await notificationCenter.add(request)
            await updatePendingRequests()
            latestMessage = "\(pattern.title) の通知を予約しました。"
        } catch {
            latestMessage = "通知の予約に失敗しました: \(error.localizedDescription)"
        }
    }

    /// 予約中の通知一覧だけを再読み込みします。
    func refreshPendingRequests() async {
        guard !isWorking else { return }

        isWorking = true
        defer { isWorking = false }

        await updateAuthorizationStatus()
        await updatePendingRequests()
        latestMessage = "予約中の通知を更新しました。"
    }

    /// 1件だけ予約を取り消します。
    func cancelPendingRequest(withIdentifier identifier: String) async {
        guard !isWorking else { return }

        isWorking = true
        defer { isWorking = false }

        notificationCenter.removePendingNotificationRequests(withIdentifiers: [identifier])
        await updatePendingRequests()
        latestMessage = "予約ID \(identifier) を取り消しました。"
    }

    /// 予約中のローカル通知をすべて取り消します。
    func cancelAllPendingRequests() async {
        guard !isWorking else { return }

        isWorking = true
        defer { isWorking = false }
        notificationCenter.removeAllPendingNotificationRequests()
        await updatePendingRequests()
        latestMessage = "予約中の通知をすべて取り消しました。"
    }

    @discardableResult
    private func updateAuthorizationStatus() async -> UNAuthorizationStatus {
        let settings = await notificationCenter.notificationSettings()
        authorizationStatusText = settings.authorizationStatus.displayText
        return settings.authorizationStatus
    }

    private func updatePendingRequests() async {
        let requests = await notificationCenter.pendingNotificationRequests()
        pendingRequests = requests
            .map(PendingNotificationItem.init)
            .sorted { firstRequest, secondRequest in
                firstRequest.id < secondRequest.id
            }
    }

    private func makeRequest(for pattern: NotificationSamplePattern) -> UNNotificationRequest {
        let content = UNMutableNotificationContent()
        content.title = pattern.notificationTitle
        content.body = pattern.notificationBody
        content.sound = .default

        let trigger: UNNotificationTrigger

        switch pattern {
        case .timeInterval:
            // 「今から何秒後」のように、現在時刻からの相対時間で通知します。
            trigger = UNTimeIntervalNotificationTrigger(timeInterval: 5, repeats: false)

        case .calendar:
            // 「次の1分ちょうど」のように、カレンダー上の年月日と時刻で通知します。
            let fireDate = Calendar.current.date(byAdding: .minute, value: 1, to: Date()) ?? Date().addingTimeInterval(60)
            var dateComponents = Calendar.current.dateComponents([.year, .month, .day, .hour, .minute], from: fireDate)
            dateComponents.second = 0
            trigger = UNCalendarNotificationTrigger(dateMatching: dateComponents, repeats: false)

        case .dailyReminder:
            // 年月日を入れず、時刻だけを指定すると「毎日その時刻」の通知になります。
            let fireDate = Calendar.current.date(byAdding: .minute, value: 2, to: Date()) ?? Date().addingTimeInterval(120)
            var dateComponents = Calendar.current.dateComponents([.hour, .minute], from: fireDate)
            dateComponents.second = 0
            trigger = UNCalendarNotificationTrigger(dateMatching: dateComponents, repeats: true)
        }

        return UNNotificationRequest(
            identifier: pattern.requestIdentifier,
            content: content,
            trigger: trigger
        )
    }
}

/// アプリが前面にあるときの通知表示を決める delegate です。
///
/// 何もしない場合、前面表示中の通知は画面上にバナーとして出ないことがあります。
/// このサンプルでは学習しやすいように、前面でもバナー・通知センター・音を許可しています。
final class LocalNotificationForegroundDelegate: NSObject, UNUserNotificationCenterDelegate {
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        completionHandler([.banner, .list, .sound])
    }
}

private extension UNAuthorizationStatus {
    var displayText: String {
        switch self {
        case .notDetermined:
            "未選択"
        case .denied:
            "拒否"
        case .authorized:
            "許可済み"
        case .provisional:
            "仮許可"
        case .ephemeral:
            "一時許可"
        @unknown default:
            "不明"
        }
    }

    var canScheduleNotifications: Bool {
        switch self {
        case .authorized, .provisional, .ephemeral:
            true
        case .notDetermined, .denied:
            false
        @unknown default:
            false
        }
    }

    var messageForBlockedScheduling: String {
        switch self {
        case .notDetermined:
            "先に「通知の許可をリクエスト」を押してください。"
        case .denied:
            "通知が拒否されています。設定アプリで通知を許可すると表示できます。"
        case .authorized, .provisional, .ephemeral:
            "通知を予約できます。"
        @unknown default:
            "この通知許可状態では予約できません。"
        }
    }
}