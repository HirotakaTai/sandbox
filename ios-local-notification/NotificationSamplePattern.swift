//
//  NotificationSamplePattern.swift
//  ios-local-notification
//
//  Created by GitHub Copilot on 2026/05/04.
//

import Foundation
import UserNotifications

/// 画面に並べるローカル通知サンプルの種類です。
///
/// `requestIdentifier` は固定値にしています。同じサンプルを何度も押したときに、
/// 古い予約を置き換えられるため、初学者が「今どの通知が予約されているか」を追いやすくなります。
enum NotificationSamplePattern: String, CaseIterable, Identifiable {
    case timeInterval
    case calendar
    case dailyReminder

    var id: String { rawValue }

    var requestIdentifier: String {
        "sample.local-notification.\(rawValue)"
    }

    var title: String {
        switch self {
        case .timeInterval:
            "5秒後に1回だけ"
        case .calendar:
            "次の1分ちょうどに1回だけ"
        case .dailyReminder:
            "毎日同じ時刻にくり返し"
        }
    }

    var learningPoint: String {
        switch self {
        case .timeInterval:
            "UNTimeIntervalNotificationTrigger の最小サンプルです。"
        case .calendar:
            "UNCalendarNotificationTrigger で日時を指定します。"
        case .dailyReminder:
            "日付を含めず、時刻だけ指定して repeats を使います。"
        }
    }

    var systemImage: String {
        switch self {
        case .timeInterval:
            "timer"
        case .calendar:
            "calendar.badge.clock"
        case .dailyReminder:
            "repeat"
        }
    }

    var notificationTitle: String {
        switch self {
        case .timeInterval:
            "5秒後のローカル通知"
        case .calendar:
            "カレンダー指定のローカル通知"
        case .dailyReminder:
            "毎日くり返すローカル通知"
        }
    }

    var notificationBody: String {
        switch self {
        case .timeInterval:
            "時間間隔トリガーで予約した通知です。"
        case .calendar:
            "年月日と時刻を指定して予約した通知です。"
        case .dailyReminder:
            "時刻だけを指定し、毎日くり返す通知です。"
        }
    }
}

/// `UNNotificationRequest` はそのままだと画面表示に少し扱いづらいため、
/// SwiftUI の `ForEach` で使いやすい形に整えた表示用モデルです。
struct PendingNotificationItem: Identifiable, Equatable {
    let id: String
    let title: String
    let body: String
    let triggerSummary: String

    init(request: UNNotificationRequest) {
        id = request.identifier
        title = request.content.title.isEmpty ? "タイトルなし" : request.content.title
        body = request.content.body.isEmpty ? "本文なし" : request.content.body
        triggerSummary = Self.describe(trigger: request.trigger)
    }

    private static func describe(trigger: UNNotificationTrigger?) -> String {
        guard let trigger else {
            return "トリガーなし"
        }

        switch trigger {
        case let timeIntervalTrigger as UNTimeIntervalNotificationTrigger:
            let nextDateText = timeIntervalTrigger.nextTriggerDate().map { shortDateTimeText(from: $0) } ?? "次回日時を取得できません"
            let seconds = Int(timeIntervalTrigger.timeInterval)
            if timeIntervalTrigger.repeats {
                return "約\(seconds)秒ごと / 次回: \(nextDateText)"
            }
            return "約\(seconds)秒後 / 次回: \(nextDateText)"

        case let calendarTrigger as UNCalendarNotificationTrigger:
            let nextDateText = calendarTrigger.nextTriggerDate().map { shortDateTimeText(from: $0) } ?? "次回日時を取得できません"
            if calendarTrigger.repeats {
                return "カレンダー指定でくり返し / 次回: \(nextDateText)"
            }
            return "カレンダー指定で1回だけ / 次回: \(nextDateText)"

        case let locationTrigger as UNLocationNotificationTrigger:
            return locationTrigger.repeats ? "場所に入るたびに通知" : "場所に入ったら1回だけ通知"

        default:
            return "その他のトリガー"
        }
    }

    private static func shortDateTimeText(from date: Date) -> String {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "ja_JP")
        formatter.dateStyle = .short
        formatter.timeStyle = .short
        return formatter.string(from: date)
    }
}