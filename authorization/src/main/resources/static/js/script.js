/**
 * WebAuthn Utility Functions
 */
const WebAuthnUtils = {
    base64urlToUint8Array(base64url) {
        const base64 = base64url.replace(/-/g, "+").replace(/_/g, "/");
        const binary = window.atob(base64);
        return Uint8Array.from(binary, c => c.charCodeAt(0));
    },

    isSupported() {
        return !!window.PublicKeyCredential && !!navigator.credentials;
    },

    unsupportedMessage() {
        if (!window.isSecureContext) {
            return "Passkeys require a secure connection (HTTPS) or localhost. Please access via the correct URL.";
        }
        return "This browser does not support passkeys.";
    },

    getCsrfHeaders() {
        const headerName = document.querySelector('meta[name="csrf-header"]')?.content;
        const token = document.querySelector('meta[name="csrf-token"]')?.content;
        if (headerName && token) {
            return { [headerName]: token };
        }
        return {};
    }
};

/**
 * Passkey Authentication (for login page)
 */
class PasskeyAuth {
    constructor({ headers, signinButton }) {
        this.headers = headers;
        this.signinButton = signinButton;
    }

    init() {
        if (!this.signinButton) return;
        this.signinButton.addEventListener("click", () => this.authenticate());
    }

    async authenticate() {
        if (!WebAuthnUtils.isSupported()) {
            alert(WebAuthnUtils.unsupportedMessage());
            return;
        }

        try {
            const optionsResponse = await fetch("/webauthn/authenticate/options", {
                method: "POST",
                headers: { ...this.headers, "content-type": "application/json" }
            }).then(r => r.json());

            const options = {
                ...optionsResponse,
                challenge: WebAuthnUtils.base64urlToUint8Array(optionsResponse.challenge),
                allowCredentials: optionsResponse.allowCredentials?.map(cred => ({
                    ...cred,
                    id: WebAuthnUtils.base64urlToUint8Array(cred.id)
                }))
            };

            const credential = await navigator.credentials.get({ publicKey: options });

            const response = await fetch("/login/webauthn", {
                method: "POST",
                headers: { ...this.headers, "content-type": "application/json" },
                body: JSON.stringify(credential)
            });

            if (response.ok) {
                window.location.href = "/";
                return;
            }
        } catch (err) {
            if (err.name === "NotAllowedError") {
                return;
            }
            if (err.name === "SecurityError") {
                alert("Passkeys are not available on this domain. Please access via the correct URL.");
                return;
            }
            console.error(err);
        }

        alert("Could not login with passkey.");
    }
}

/**
 * Passkey Management (for passkeys page)
 */
class PasskeyManager {
    constructor({ headers, modal, passkeyNameInput, addPasskeyButton, addButtons, closeButtons, deleteButtons }) {
        this.headers = headers;
        this.modal = modal;
        this.passkeyNameInput = passkeyNameInput;
        this.addPasskeyButton = addPasskeyButton;
        this.addButtons = addButtons;
        this.closeButtons = closeButtons;
        this.deleteButtons = deleteButtons;
    }

    init() {
        this.addButtons.forEach(btn => btn.addEventListener("click", () => this.showModal()));
        this.closeButtons.forEach(btn => btn.addEventListener("click", e => this.hideModal(e)));
        this.addPasskeyButton?.addEventListener("click", () => this.addPasskey());
        this.passkeyNameInput?.addEventListener("keydown", e => {
            if (e.key === "Enter") {
                e.preventDefault();
                this.addPasskey();
            }
        });
        this.deleteButtons.forEach(btn => btn.addEventListener("click", e => this.deletePasskey(e)));
    }

    showModal() {
        if (this.modal) this.modal.style.display = "flex";
    }

    hideModal(event) {
        if (event && event.target !== event.currentTarget) return;
        if (this.modal) this.modal.style.display = "none";
        if (this.passkeyNameInput) this.passkeyNameInput.value = "";
    }

    async addPasskey() {
        if (!WebAuthnUtils.isSupported()) {
            alert(WebAuthnUtils.unsupportedMessage());
            return;
        }

        const label = this.passkeyNameInput?.value;
        if (!label) {
            alert("Please enter a label for the passkey.");
            return;
        }

        try {
            const optionsRes = await fetch("/webauthn/register/options", {
                method: "POST",
                headers: this.headers
            });
            if (!optionsRes.ok) {
                throw new Error("Failed to get registration options (status:" + optionsRes.status + ")");
            }
            const optionsResponse = await optionsRes.json();

            const options = {
                ...optionsResponse,
                excludeCredentials: [],
                user: {
                    ...optionsResponse.user,
                    id: WebAuthnUtils.base64urlToUint8Array(optionsResponse.user.id)
                },
                challenge: WebAuthnUtils.base64urlToUint8Array(optionsResponse.challenge)
            };

            const credential = await navigator.credentials.create({ publicKey: options });

            const registerRes = await fetch("/webauthn/register", {
                method: "POST",
                headers: { ...this.headers, "content-type": "application/json" },
                body: JSON.stringify({ publicKey: { credential, label } })
            });
            if (!registerRes.ok) {
                throw new Error("Failed to register passkey (status:" + registerRes.status + ")");
            }

            window.location.href = window.location.pathname + "?success";
        } catch (e) {
            if (e.name === "NotAllowedError") {
                return;
            }
            if (e.name === "SecurityError") {
                alert("Passkeys are not available on this domain. Please access via the correct URL.");
                return;
            }
            console.error("Could not register passkey", e);
            alert("Could not register passkey: " + e.message);
        }

        this.hideModal();
    }

    async deletePasskey(evt) {
        const id = evt.target.getAttribute("data-id");
        if (!confirm("Are you sure you want to delete this passkey?")) return;

        const response = await fetch(`/webauthn/register/${id}`, {
            method: "DELETE",
            headers: this.headers
        });

        if (response.status === 204) {
            evt.target.closest(".passkey-item").remove();
        } else {
            alert(`Failed to delete passkey (status:${response.status})`);
        }
    }
}

/**
 * Initialize based on current page
 */
document.addEventListener("DOMContentLoaded", () => {
    const headers = WebAuthnUtils.getCsrfHeaders();

    // Login page: passkey sign-in button
    const signinButton = document.getElementById("passkey-signin");
    if (signinButton) {
        const auth = new PasskeyAuth({ headers, signinButton });
        auth.init();
    }

    // Passkeys management page
    const addModal = document.getElementById("add-modal");
    if (addModal) {
        const manager = new PasskeyManager({
            headers,
            modal: addModal,
            passkeyNameInput: document.getElementById("passkey-name"),
            addPasskeyButton: document.getElementById("btn-add-passkey"),
            addButtons: document.querySelectorAll(".btn-add"),
            closeButtons: document.querySelectorAll(".modal-close"),
            deleteButtons: document.querySelectorAll(".delete-icon")
        });
        manager.init();
    }
});
