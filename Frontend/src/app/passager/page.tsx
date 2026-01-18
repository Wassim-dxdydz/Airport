"use client";

import { useEffect, useState } from "react";
import { Button } from "@/components/ui/button";
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from "@/components/ui/table";
import { Card } from "@/components/ui/card";
import { Alert, AlertDescription } from "@/components/ui/alert";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import {
    MoreHorizontal,
    Plus,
    Pencil,
    Trash2,
    RefreshCw,
    CheckCircle2,
    XCircle,
    Search,
} from "lucide-react";

const API_BASE = process.env.NEXT_PUBLIC_API_BASE ?? "";

type Passenger = {
    id: string;
    prenom: string;
    nom: string;
    email: string;
    telephone: string | null;
};

type CreatePassengerPayload = {
    prenom: string;
    nom: string;
    email: string;
    telephone?: string;
};

type UpdatePassengerPayload = {
    prenom?: string;
    nom?: string;
    email?: string;
    telephone?: string;
};

type Notification = {
    type: 'success' | 'error';
    message: string;
} | null;

type ValidationErrors = {
    prenom?: string;
    nom?: string;
    email?: string;
    telephone?: string;
};

async function api<T>(path: string, init?: RequestInit): Promise<T> {
    const res = await fetch(`${API_BASE}${path}`, {
        ...init,
        headers: { "Content-Type": "application/json", ...(init?.headers || {}) },
        cache: "no-store",
    });
    if (!res.ok) {
        try {
            const errorJson = await res.json();
            throw new Error(errorJson.message || errorJson.error || "Erreur HTTP");
        } catch (parseError: any) {
            if (parseError instanceof Error && parseError.message !== "Erreur HTTP") {
                throw parseError;
            }
            throw new Error("Erreur HTTP");
        }
    }
    return res.json();
}

export default function PassagerPage() {
    const [allPassengers, setAllPassengers] = useState<Passenger[]>([]);
    const [displayedPassengers, setDisplayedPassengers] = useState<Passenger[]>([]);
    const [loading, setLoading] = useState(true);
    const [notification, setNotification] = useState<Notification>(null);
    const [searchTerm, setSearchTerm] = useState("");

    const [openCreate, setOpenCreate] = useState(false);
    const [openEdit, setOpenEdit] = useState<Passenger | null>(null);

    const [createForm, setCreateForm] = useState<CreatePassengerPayload>({
        prenom: "",
        nom: "",
        email: "",
        telephone: "",
    });

    const [editForm, setEditForm] = useState<UpdatePassengerPayload>({
        prenom: "",
        nom: "",
        email: "",
        telephone: "",
    });

    const [validationErrors, setValidationErrors] = useState<ValidationErrors>({});

    const showNotification = (type: 'success' | 'error', message: string) => {
        setNotification({ type, message });
        setTimeout(() => setNotification(null), 5000);
    };

    const load = async () => {
        try {
            setLoading(true);
            const data = await api<Passenger[]>("/api/passagers");
            setAllPassengers(data);
        } catch (e: any) {
            showNotification('error', e.message);
        }
        setLoading(false);
    };

    useEffect(() => {
        load();
    }, []);

    useEffect(() => {
        const term = searchTerm.toLowerCase();
        const filtered = allPassengers.filter(
            (p) =>
                p.prenom.toLowerCase().includes(term) ||
                p.nom.toLowerCase().includes(term) ||
                p.email.toLowerCase().includes(term) ||
                p.telephone?.toLowerCase().includes(term)
        );
        setDisplayedPassengers(filtered);
    }, [allPassengers, searchTerm]);

    const setCreateField = (k: string, v: any) => {
        setCreateForm((f) => ({ ...f, [k]: v }));
        if (validationErrors[k as keyof ValidationErrors]) {
            setValidationErrors((prev) => ({ ...prev, [k]: undefined }));
        }
    };

    const resetCreateForm = () => {
        setCreateForm({
            prenom: "",
            nom: "",
            email: "",
            telephone: "",
        });
        setValidationErrors({});
    };

    const createPassenger = async () => {
        try {
            const payload: CreatePassengerPayload = {
                prenom: createForm.prenom.trim(),
                nom: createForm.nom.trim(),
                email: createForm.email.trim().toLowerCase(),
                telephone: createForm.telephone?.trim() || undefined,
            };

            await api<Passenger>("/api/passagers", { method: "POST", body: JSON.stringify(payload) });
            setOpenCreate(false);
            resetCreateForm();
            showNotification('success', 'Passager créé avec succès');
            await load();
        } catch (e: any) {
            const errorMessage = e.message.toLowerCase();
            const errors: ValidationErrors = {};

            if (errorMessage.includes('prénom') || errorMessage.includes('prenom')) {
                errors.prenom = e.message;
            } else if (errorMessage.includes('nom') && !errorMessage.includes('prénom')) {
                errors.nom = e.message;
            } else if (errorMessage.includes('email') || errorMessage.includes('existe déjà')) {
                errors.email = e.message;
            } else if (errorMessage.includes('téléphone') || errorMessage.includes('telephone')) {
                errors.telephone = e.message;
            } else {
                showNotification('error', e.message);
                return;
            }

            setValidationErrors(errors);
        }
    };

    const startEdit = (p: Passenger) => {
        setOpenEdit(p);
        setEditForm({
            prenom: p.prenom,
            nom: p.nom,
            email: p.email,
            telephone: p.telephone || "",
        });
        setValidationErrors({});
    };

    const updatePassenger = async (id: string) => {
        try {
            const payload: UpdatePassengerPayload = {
                prenom: editForm.prenom?.trim(),
                nom: editForm.nom?.trim(),
                email: editForm.email?.trim().toLowerCase(),
                telephone: editForm.telephone?.trim() || undefined,
            };

            await api<Passenger>(`/api/passagers/${id}`, { method: "PATCH", body: JSON.stringify(payload) });
            setOpenEdit(null);
            setValidationErrors({});
            showNotification('success', 'Passager modifié avec succès');
            await load();
        } catch (e: any) {
            const errorMessage = e.message.toLowerCase();
            const errors: ValidationErrors = {};

            if (errorMessage.includes('prénom') || errorMessage.includes('prenom')) {
                errors.prenom = e.message;
            } else if (errorMessage.includes('nom')) {
                errors.nom = e.message;
            } else if (errorMessage.includes('email') || errorMessage.includes('existe déjà')) {
                errors.email = e.message;
            } else if (errorMessage.includes('téléphone') || errorMessage.includes('telephone')) {
                errors.telephone = e.message;
            } else {
                showNotification('error', e.message);
                return;
            }

            setValidationErrors(errors);
        }
    };

    const deletePassenger = async (id: string, nom: string, prenom: string) => {
        if (!confirm(`Supprimer le passager ${prenom} ${nom} ?`)) return;
        try {
            await fetch(`${API_BASE}/api/passagers/${id}`, { method: "DELETE" });
            showNotification('success', 'Passager supprimé avec succès');
            await load();
        } catch (e: any) {
            showNotification('error', e.message);
        }
    };

    return (
        <div className="mx-auto max-w-7xl px-4 py-8 space-y-6">
            <div className="flex flex-wrap items-center justify-between gap-3">
                <div>
                    <h1 className="text-2xl font-semibold">Passagers</h1>
                    <p className="text-muted-foreground">Gestion de la base de données des clients</p>
                </div>

                <div className="flex flex-wrap items-center gap-2">
                    <Button variant="outline" onClick={load}>
                        <RefreshCw className="mr-2 h-4 w-4" />
                        Actualiser
                    </Button>

                    <Button onClick={() => { resetCreateForm(); setOpenCreate(true); }}>
                        <Plus className="mr-2 h-4 w-4" />
                        Nouveau passager
                    </Button>
                </div>
            </div>

            {notification && (
                <Alert
                    className={
                        notification.type === 'success'
                            ? "border-green-300 bg-green-50"
                            : "border-red-300 bg-red-50"
                    }
                >
                    {notification.type === 'success' ? (
                        <CheckCircle2 className="h-4 w-4 text-green-600" />
                    ) : (
                        <XCircle className="h-4 w-4 text-red-600" />
                    )}
                    <AlertDescription
                        className={
                            notification.type === 'success'
                                ? "text-green-800"
                                : "text-red-800"
                        }
                    >
                        {notification.message}
                    </AlertDescription>
                </Alert>
            )}

            {/* Search Bar - Outside Card */}
            <div className="relative max-w-md">
                <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                <Input
                    placeholder="Rechercher par nom, prénom, email ou téléphone..."
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    className="pl-10"
                />
            </div>

            <Card className="overflow-hidden py-0">
                <div className="overflow-x-auto px-4 py-2">
                    <Table>
                        <TableHeader>
                            <TableRow>
                                <TableHead>Prénom</TableHead>
                                <TableHead>Nom</TableHead>
                                <TableHead>Email</TableHead>
                                <TableHead>Téléphone</TableHead>
                                <TableHead className="text-right">Actions</TableHead>
                            </TableRow>
                        </TableHeader>
                        <TableBody>
                            {loading ? (
                                <TableRow>
                                    <TableCell colSpan={5}>Chargement…</TableCell>
                                </TableRow>
                            ) : displayedPassengers.length === 0 ? (
                                <TableRow>
                                    <TableCell colSpan={5} className="text-muted-foreground">
                                        {searchTerm ? "Aucun passager trouvé" : "Aucun passager enregistré"}
                                    </TableCell>
                                </TableRow>
                            ) : (
                                displayedPassengers.map((p) => (
                                    <TableRow key={p.id}>
                                        <TableCell className="font-medium">{p.prenom}</TableCell>
                                        <TableCell>{p.nom}</TableCell>
                                        <TableCell>{p.email}</TableCell>
                                        <TableCell>{p.telephone || "—"}</TableCell>
                                        <TableCell className="text-right">
                                            <DropdownMenu>
                                                <DropdownMenuTrigger asChild>
                                                    <Button size="icon" variant="ghost">
                                                        <MoreHorizontal className="h-4 w-4" />
                                                    </Button>
                                                </DropdownMenuTrigger>
                                                <DropdownMenuContent align="end">
                                                    <DropdownMenuItem onClick={() => startEdit(p)}>
                                                        <Pencil className="mr-2 h-4 w-4" /> Modifier
                                                    </DropdownMenuItem>
                                                    <DropdownMenuItem
                                                        className="text-red-600"
                                                        onClick={() => deletePassenger(p.id, p.nom, p.prenom)}
                                                    >
                                                        <Trash2 className="mr-2 h-4 w-4" /> Supprimer
                                                    </DropdownMenuItem>
                                                </DropdownMenuContent>
                                            </DropdownMenu>
                                        </TableCell>
                                    </TableRow>
                                ))
                            )}
                        </TableBody>
                    </Table>
                </div>
            </Card>

            {/* CREATE DIALOG */}
            <Dialog open={openCreate} onOpenChange={(o) => {
                if (!o) {
                    setOpenCreate(false);
                    setValidationErrors({});
                }
            }}>
                <DialogContent className="max-w-2xl">
                    <DialogHeader>
                        <DialogTitle>Nouveau passager</DialogTitle>
                        <DialogDescription>Renseignez les informations du passager</DialogDescription>
                    </DialogHeader>
                    <div className="grid gap-4 py-2">
                        <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
                            <div className="space-y-2">
                                <Label>Prénom</Label>
                                <Input
                                    placeholder="Jean"
                                    value={createForm.prenom}
                                    onChange={(e) => setCreateField("prenom", e.target.value)}
                                    className={validationErrors.prenom ? "border-red-500" : ""}
                                />
                                {validationErrors.prenom && (
                                    <p className="text-sm text-red-600">{validationErrors.prenom}</p>
                                )}
                            </div>
                            <div className="space-y-2">
                                <Label>Nom</Label>
                                <Input
                                    placeholder="Dupont"
                                    value={createForm.nom}
                                    onChange={(e) => setCreateField("nom", e.target.value)}
                                    className={validationErrors.nom ? "border-red-500" : ""}
                                />
                                {validationErrors.nom && (
                                    <p className="text-sm text-red-600">{validationErrors.nom}</p>
                                )}
                            </div>
                        </div>

                        <div className="space-y-2">
                            <Label>Email</Label>
                            <Input
                                type="email"
                                placeholder="jean.dupont@example.com"
                                value={createForm.email}
                                onChange={(e) => setCreateField("email", e.target.value)}
                                className={validationErrors.email ? "border-red-500" : ""}
                            />
                            {validationErrors.email && (
                                <p className="text-sm text-red-600">{validationErrors.email}</p>
                            )}
                        </div>

                        <div className="space-y-2">
                            <Label>Téléphone (optionnel)</Label>
                            <Input
                                placeholder="+33612345678 ou 0612345678"
                                value={createForm.telephone}
                                onChange={(e) => setCreateField("telephone", e.target.value)}
                                className={validationErrors.telephone ? "border-red-500" : ""}
                            />
                            {validationErrors.telephone && (
                                <p className="text-sm text-red-600">{validationErrors.telephone}</p>
                            )}
                        </div>
                    </div>
                    <DialogFooter>
                        <Button variant="outline" onClick={() => {
                            setOpenCreate(false);
                            setValidationErrors({});
                        }}>
                            Annuler
                        </Button>
                        <Button onClick={createPassenger}>
                            Enregistrer
                        </Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>

            {/* EDIT DIALOG */}
            <Dialog open={!!openEdit} onOpenChange={(o) => {
                if (!o) {
                    setOpenEdit(null);
                    setValidationErrors({});
                }
            }}>
                <DialogContent className="max-w-2xl">
                    <DialogHeader>
                        <DialogTitle>Modifier le passager {openEdit?.prenom} {openEdit?.nom}</DialogTitle>
                    </DialogHeader>
                    <div className="grid gap-4 py-2">
                        <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
                            <div className="space-y-2">
                                <Label>Prénom</Label>
                                <Input
                                    value={editForm.prenom}
                                    onChange={(e) => setEditForm((f) => ({ ...f, prenom: e.target.value }))}
                                    className={validationErrors.prenom ? "border-red-500" : ""}
                                />
                                {validationErrors.prenom && (
                                    <p className="text-sm text-red-600">{validationErrors.prenom}</p>
                                )}
                            </div>
                            <div className="space-y-2">
                                <Label>Nom</Label>
                                <Input
                                    value={editForm.nom}
                                    onChange={(e) => setEditForm((f) => ({ ...f, nom: e.target.value }))}
                                    className={validationErrors.nom ? "border-red-500" : ""}
                                />
                                {validationErrors.nom && (
                                    <p className="text-sm text-red-600">{validationErrors.nom}</p>
                                )}
                            </div>
                        </div>

                        <div className="space-y-2">
                            <Label>Email</Label>
                            <Input
                                type="email"
                                value={editForm.email}
                                onChange={(e) => setEditForm((f) => ({ ...f, email: e.target.value }))}
                                className={validationErrors.email ? "border-red-500" : ""}
                            />
                            {validationErrors.email && (
                                <p className="text-sm text-red-600">{validationErrors.email}</p>
                            )}
                        </div>

                        <div className="space-y-2">
                            <Label>Téléphone (optionnel)</Label>
                            <Input
                                value={editForm.telephone}
                                onChange={(e) => setEditForm((f) => ({ ...f, telephone: e.target.value }))}
                                className={validationErrors.telephone ? "border-red-500" : ""}
                            />
                            {validationErrors.telephone && (
                                <p className="text-sm text-red-600">{validationErrors.telephone}</p>
                            )}
                        </div>
                    </div>
                    <DialogFooter>
                        <Button variant="outline" onClick={() => {
                            setOpenEdit(null);
                            setValidationErrors({});
                        }}>
                            Annuler
                        </Button>
                        {openEdit && (
                            <Button onClick={() => updatePassenger(openEdit.id)}>
                                Enregistrer
                            </Button>
                        )}
                    </DialogFooter>
                </DialogContent>
            </Dialog>
        </div>
    );
}
