//
//  ContentView.swift
//  ios-local-notification
//
//  Created by Hirotaka Tai on 2026/05/04.
//

import SwiftUI

struct ContentView: View {
    @EnvironmentObject private var notificationManager: LocalNotificationManager

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    headerSection
                    permissionSection
                    scheduleSection
                    pendingSection
                    learnerNoteSection
                }
                .padding()
            }
            .background(Color(.systemGroupedBackground))
            .navigationTitle("ローカル通知")
            .task {
                await notificationManager.refreshAll()
            }
        }
    }

    private var headerSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            Label("Local Notifications", systemImage: "bell.badge")
                .font(.title2.bold())

            Text("権限を確認し、3種類のローカル通知を予約して、予約一覧とキャンセル動作を試せます。")
                .font(.subheadline)
                .foregroundStyle(.secondary)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }

    private var permissionSection: some View {
        SampleSection(title: "1. 権限と状態", systemImage: "checkmark.shield") {
            StatusRow(title: "通知許可", value: notificationManager.authorizationStatusText)
            StatusRow(title: "予約中", value: "\(notificationManager.pendingRequests.count)件")

            Text(notificationManager.latestMessage)
                .font(.footnote)
                .foregroundStyle(.secondary)
                .frame(maxWidth: .infinity, alignment: .leading)

            HStack(spacing: 12) {
                Button {
                    Task { await notificationManager.requestAuthorization() }
                } label: {
                    Label("許可をリクエスト", systemImage: "bell.badge")
                }
                .buttonStyle(.borderedProminent)

                Button {
                    Task { await notificationManager.refreshAll() }
                } label: {
                    Label("再読み込み", systemImage: "arrow.clockwise")
                }
                .buttonStyle(.bordered)
            }
            .disabled(notificationManager.isWorking)
        }
    }

    private var scheduleSection: some View {
        SampleSection(title: "2. 予約パターン", systemImage: "calendar.badge.plus") {
            ForEach(NotificationSamplePattern.allCases) { pattern in
                Button {
                    Task { await notificationManager.schedule(pattern) }
                } label: {
                    PatternButtonLabel(pattern: pattern)
                }
                .buttonStyle(.plain)
                .disabled(notificationManager.isWorking)
            }
        }
    }

    private var pendingSection: some View {
        SampleSection(title: "3. 予約一覧とキャンセル", systemImage: "list.bullet.rectangle") {
            if notificationManager.pendingRequests.isEmpty {
                Text("予約中の通知はありません。")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
                    .frame(maxWidth: .infinity, alignment: .leading)
            } else {
                ForEach(notificationManager.pendingRequests) { pendingRequest in
                    PendingRequestRow(pendingRequest: pendingRequest) {
                        Task {
                            await notificationManager.cancelPendingRequest(withIdentifier: pendingRequest.id)
                        }
                    }
                }
            }

            HStack(spacing: 12) {
                Button {
                    Task { await notificationManager.refreshPendingRequests() }
                } label: {
                    Label("一覧を更新", systemImage: "arrow.clockwise")
                }
                .buttonStyle(.bordered)

                Button(role: .destructive) {
                    Task { await notificationManager.cancelAllPendingRequests() }
                } label: {
                    Label("すべて取消", systemImage: "trash")
                }
                .buttonStyle(.bordered)
                .disabled(notificationManager.pendingRequests.isEmpty)
            }
            .disabled(notificationManager.isWorking)
        }
    }

    private var learnerNoteSection: some View {
        SampleSection(title: "学習メモ", systemImage: "lightbulb") {
            Text("アプリを開いたまま通知時刻を迎えても表示されるように、UNUserNotificationCenterDelegate で前面表示を許可しています。")
                .font(.footnote)
                .foregroundStyle(.secondary)
        }
    }
}

private struct SampleSection<Content: View>: View {
    let title: String
    let systemImage: String
    private let content: Content

    init(title: String, systemImage: String, @ViewBuilder content: () -> Content) {
        self.title = title
        self.systemImage = systemImage
        self.content = content()
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Label(title, systemImage: systemImage)
                .font(.headline)

            content
        }
        .padding()
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color(.secondarySystemGroupedBackground), in: RoundedRectangle(cornerRadius: 12))
    }
}

private struct StatusRow: View {
    let title: String
    let value: String

    var body: some View {
        HStack {
            Text(title)
                .foregroundStyle(.secondary)

            Spacer()

            Text(value)
                .fontWeight(.semibold)
        }
        .font(.subheadline)
    }
}

private struct PatternButtonLabel: View {
    let pattern: NotificationSamplePattern

    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: pattern.systemImage)
                .font(.title3)
                .foregroundStyle(.tint)
                .frame(width: 28)

            VStack(alignment: .leading, spacing: 4) {
                Text(pattern.title)
                    .font(.subheadline.weight(.semibold))
                    .foregroundStyle(.primary)

                Text(pattern.learningPoint)
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }

            Spacer()

            Image(systemName: "plus.circle.fill")
                .foregroundStyle(.tint)
        }
        .padding(12)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color(.tertiarySystemGroupedBackground), in: RoundedRectangle(cornerRadius: 10))
    }
}

private struct PendingRequestRow: View {
    let pendingRequest: PendingNotificationItem
    let cancelAction: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack(alignment: .top) {
                VStack(alignment: .leading, spacing: 4) {
                    Text(pendingRequest.title)
                        .font(.subheadline.weight(.semibold))

                    Text(pendingRequest.body)
                        .font(.caption)
                        .foregroundStyle(.secondary)

                    Text(pendingRequest.triggerSummary)
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }

                Spacer()

                Button(role: .destructive, action: cancelAction) {
                    Image(systemName: "xmark.circle.fill")
                        .imageScale(.large)
                }
                .buttonStyle(.plain)
                .accessibilityLabel("この通知を取り消す")
            }

            Text(pendingRequest.id)
                .font(.caption2.monospaced())
                .foregroundStyle(.tertiary)
        }
        .padding(12)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color(.tertiarySystemGroupedBackground), in: RoundedRectangle(cornerRadius: 10))
    }
}

#Preview {
    ContentView()
        .environmentObject(LocalNotificationManager())
}
