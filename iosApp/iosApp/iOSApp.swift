import SwiftUI
import shared
import GoogleCast
import Network

@main
struct iOSApp: App {

    init() {
        MainViewControllerKt.doInitKoin()

        // iOS 14+ only shows an app in Settings → Privacy → Local Network (and
        // presents the permission prompt) after the app has actually sent a packet
        // onto the local network. Passive Bonjour browsing declared in
        // NSBonjourServices goes through the system mDNS daemon and never triggers
        // the prompt on its own, so without this the app never appears in the list
        // and Cast SDK cannot get full local-network access to discover Chromecast
        // devices. Sending a tiny UDP datagram to the broadcast address forces iOS
        // to show the dialog immediately on first launch.
        triggerLocalNetworkPermission()

        // Initialize Google Cast
        let discoveryCriteria = GCKDiscoveryCriteria(applicationID: kGCKDefaultMediaReceiverApplicationID)
        let options = GCKCastOptions(discoveryCriteria: discoveryCriteria)
        options.startDiscoveryAfterFirstTapOnCastButton = false
        GCKCastContext.setSharedInstanceWith(options)
        // Show the built-in expanded media controls (transport bar + "Stop casting" button)
        // when the Cast button is tapped during an active session.
        GCKCastContext.sharedInstance().useDefaultExpandedMediaControls = true
        GCKCastContext.sharedInstance().discoveryManager.startDiscovery()
    }

    /// Sends a no-op UDP datagram to the broadcast address so that iOS registers
    /// the app as a local-network user and shows the permission prompt.
    private func triggerLocalNetworkPermission() {
        let connection = NWConnection(
            host: "255.255.255.255",
            port: 9,          // discard port — the datagram is silently dropped
            using: .udp
        )
        connection.start(queue: .main)
        connection.send(content: Data([0]), completion: .idempotent)
        connection.cancel()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
