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
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "@/components/ui/select";
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
    Filter,
    ArrowUpDown,
    ArrowUp,
    ArrowDown,
} from "lucide-react";

const API_BASE = process.env.NEXT_PUBLIC_API_BASE ?? "";

type CheckIn = {
    id: string;
    passagerId: string;
    volId: string;
    numeroSiege: string;
    heureCheckIn: string;
    passagerInfo: {
        id: string;
        prenom: string;
        nom: string;
        email: string;
        telephone: string | null;
    } | null;
};

type Passenger = {
    id: string;
    prenom: string;
    nom: string;
    email: string;
    telephone: string | null;
};

type Vol = {
    id: string;
    numeroVol: string;
    origine: string;
    destination: string;
    heureDepart: string;
    heureArrivee: string;
    etat: string;
    avionId: string | null;
};

type CreateCheckInPayload = {
    passagerId: string;
    volId: string;
    numeroSiege: string;
};

type UpdateCheckInPayload = {
    numeroSiege?: string;
};

type Notification = {
    type: 'success' | 'error';
    message: string;
} | null;

type ValidationErrors = {
    passagerId?: string;
    volId?: string;
    numeroSiege?: string;
};

type SortConfig = {
    key: 'passager' | 'vol' | 'heureCheckIn' | null;
    direction: 'asc' | 'desc' | null;
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

const fmt = new Intl.DateTimeFormat("fr-FR", {
    dateStyle: "short",
    timeStyle: "short",
});

export default function CheckInPage() {
    const [allCheckIns, setAllCheckIns] = useState<CheckIn[]>([]);
    const [displayedCheckIns, setDisplayedCheckIns] = useState<CheckIn[]>([]);
    const [passengers, setPassengers] = useState<Passenger[]>([]);
    const [vols, setVols] = useState<Vol[]>([]);
    const [loading, setLoading] = useState(true);
    const [notification, setNotification] = useState<Notification>(null);
    const [volFilter, setVolFilter] = useState<string>("ALL");
    const [sortConfig, setSortConfig] = useState<SortConfig>({ key: null, direction: null });

    const [openCreate, setOpenCreate] = useState(false);
    const [openEdit, setOpenEdit] = useState<CheckIn | null>(null);

    const [createForm, setCreateForm] = useState<CreateCheckInPayload>({
        passagerId: "",
        volId: "",
        numeroSiege: "",
    });

    const [editForm, setEditForm] = useState<UpdateCheckInPayload>({
        numeroSiege: "",
    });

    const [validationErrors, setValidationErrors] = useState<ValidationErrors>({});

    const showNotification = (type: 'success' | 'error', message: string) => {
        setNotification({ type, message });
        setTimeout(() => setNotification(null), 5000);
    };

    const load = async () => {
        try {
            setLoading(true);
            const [checkInsData, passengersData, volsData] = await Promise.all([
                api<CheckIn[]>("/api/checkins"),
                api<Passenger[]>("/api/passagers"),
                api<Vol[]>("/api/vols")
            ]);

            setAllCheckIns(checkInsData);
            setPassengers(passengersData);
            setVols(volsData.filter(v =>
                v.etat.toUpperCase() === "PREVU" ||
                v.etat.toUpperCase() === "EMBARQUEMENT"
            ));
        } catch (e: any) {
            showNotification('error', e.message);
        }
        setLoading(false);
    };

    useEffect(() => {
        load();
    }, []);

    useEffect(() => {
        let filtered = [...allCheckIns];

        if (volFilter !== "ALL") {
            filtered = filtered.filter(c => c.volId === volFilter);
        }

        // Apply sorting
        if (sortConfig.key && sortConfig.direction) {
            filtered.sort((a, b) => {
                let aValue: any;
                let bValue: any;

                if (sortConfig.key === 'passager') {
                    aValue = getPassagerName(a);
                    bValue = getPassagerName(b);
                } else if (sortConfig.key === 'vol') {
                    aValue = getVolNumero(a.volId);
                    bValue = getVolNumero(b.volId);
                } else if (sortConfig.key === 'heureCheckIn') {
                    aValue = new Date(a.heureCheckIn).getTime();
                    bValue = new Date(b.heureCheckIn).getTime();
                }

                if (typeof aValue === 'string') {
                    return sortConfig.direction === 'asc'
                        ? aValue.localeCompare(bValue)
                        : bValue.localeCompare(aValue);
                } else {
                    return sortConfig.direction === 'asc'
                        ? aValue - bValue
                        : bValue - aValue;
                }
            });
        }

        setDisplayedCheckIns(filtered);
    }, [allCheckIns, volFilter, sortConfig]);

    const handleSort = (key: 'passager' | 'vol' | 'heureCheckIn') => {
        setSortConfig(prev => {
            if (prev.key !== key) {
                return { key, direction: 'asc' };
            }
            if (prev.direction === 'asc') {
                return { key, direction: 'desc' };
            }
            if (prev.direction === 'desc') {
                return { key: null, direction: null };
            }
            return { key, direction: 'asc' };
        });
    };

    const getSortIcon = (key: 'passager' | 'vol' | 'heureCheckIn') => {
        if (sortConfig.key !== key || sortConfig.direction === null) {
            return <ArrowUpDown className="ml-2 h-4 w-4 inline-block" />;
        }
        return sortConfig.direction === 'asc'
            ? <ArrowUp className="ml-2 h-4 w-4 inline-block" />
            : <ArrowDown className="ml-2 h-4 w-4 inline-block" />;
    };

    const availableVols = vols.filter(v => v.avionId !== null);

    const getVolNumero = (volId: string): string => {
        const vol = vols.find(v => v.id === volId);
        return vol?.numeroVol || volId.slice(0, 8) + "...";
    };

    const getPassagerName = (checkIn: CheckIn): string => {
        if (checkIn.passagerInfo) {
            return `${checkIn.passagerInfo.prenom} ${checkIn.passagerInfo.nom}`;
        }
        const passenger = passengers.find(p => p.id === checkIn.passagerId);
        return passenger ? `${passenger.prenom} ${passenger.nom}` : "—";
    };

    const setCreateField = (k: string, v: any) => {
        setCreateForm((f) => ({ ...f, [k]: v }));
        if (validationErrors[k as keyof ValidationErrors]) {
            setValidationErrors((prev) => ({ ...prev, [k]: undefined }));
        }
    };

    const resetCreateForm = () => {
        setCreateForm({
            passagerId: "",
            volId: "",
            numeroSiege: "",
        });
        setValidationErrors({});
    };

    const createCheckIn = async () => {
        try {
            const payload: CreateCheckInPayload = {
                passagerId: createForm.passagerId,
                volId: createForm.volId,
                numeroSiege: createForm.numeroSiege.trim().toUpperCase(),
            };

            await api<CheckIn>("/api/checkins", { method: "POST", body: JSON.stringify(payload) });
            setOpenCreate(false);
            resetCreateForm();
            showNotification('success', 'Check-in créé avec succès');
            await load();
        } catch (e: any) {
            const errorMessage = e.message.toLowerCase();
            const errors: ValidationErrors = {};

            if (errorMessage.includes('passager') && errorMessage.includes('non trouvé')) {
                errors.passagerId = e.message;
            } else if (errorMessage.includes('vol') && errorMessage.includes('non trouvé')) {
                errors.volId = e.message;
            } else if (errorMessage.includes('siège') || errorMessage.includes('siege') ||
                errorMessage.includes('format') || errorMessage.includes('rangée') ||
                errorMessage.includes('occupé') || errorMessage.includes('colonne')) {
                errors.numeroSiege = e.message;
            } else if (errorMessage.includes('déjà enregistré') || errorMessage.includes('deja enregistre')) {
                errors.passagerId = e.message;
            } else if (errorMessage.includes('impossible') && errorMessage.includes('enregistrer')) {
                errors.volId = e.message;
            } else {
                showNotification('error', e.message);
                return;
            }

            setValidationErrors(errors);
        }
    };

    const startEdit = (c: CheckIn) => {
        setOpenEdit(c);
        setEditForm({
            numeroSiege: c.numeroSiege,
        });
        setValidationErrors({});
    };

    const updateCheckIn = async (id: string) => {
        try {
            const payload: UpdateCheckInPayload = {
                numeroSiege: editForm.numeroSiege?.trim().toUpperCase(),
            };

            await api<CheckIn>(`/api/checkins/${id}`, { method: "PATCH", body: JSON.stringify(payload) });
            setOpenEdit(null);
            setValidationErrors({});
            showNotification('success', 'Check-in modifié avec succès');
            await load();
        } catch (e: any) {
            const errorMessage = e.message.toLowerCase();
            const errors: ValidationErrors = {};

            if (errorMessage.includes('siège') || errorMessage.includes('siege') ||
                errorMessage.includes('format') || errorMessage.includes('rangée') ||
                errorMessage.includes('occupé') || errorMessage.includes('colonne')) {
                errors.numeroSiege = e.message;
            } else {
                showNotification('error', e.message);
                return;
            }

            setValidationErrors(errors);
        }
    };

    const deleteCheckIn = async (id: string, passagerName: string) => {
        if (!confirm(`Supprimer le check-in de ${passagerName} ?`)) return;
        try {
            await fetch(`${API_BASE}/api/checkins/${id}`, { method: "DELETE" });
            showNotification('success', 'Check-in supprimé avec succès');
            await load();
        } catch (e: any) {
            showNotification('error', e.message);
        }
    };

    return (
        <div className="mx-auto max-w-7xl px-4 py-8 space-y-6">
            <div className="flex flex-wrap items-center justify-between gap-3">
                <div>
                    <h1 className="text-2xl font-semibold">Check-ins</h1>
                    <p className="text-muted-foreground">Enregistrement des passagers et attribution des sièges</p>
                </div>

                <div className="flex flex-wrap items-center gap-2">
                    <Select value={volFilter} onValueChange={setVolFilter}>
                        <SelectTrigger className="w-[200px]">
                            <SelectValue placeholder="Filtrer par vol" />
                        </SelectTrigger>
                        <SelectContent>
                            <SelectItem value="ALL">Tous les vols</SelectItem>
                            {vols.map((v) => (
                                <SelectItem key={v.id} value={v.id}>
                                    {v.numeroVol} ({v.origine} → {v.destination})
                                </SelectItem>
                            ))}
                        </SelectContent>
                    </Select>

                    <Button variant="outline" onClick={load}>
                        <RefreshCw className="mr-2 h-4 w-4" />
                        Actualiser
                    </Button>

                    <Button onClick={() => { resetCreateForm(); setOpenCreate(true); }}>
                        <Plus className="mr-2 h-4 w-4" />
                        Nouveau check-in
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

            <Card className="overflow-hidden py-0">
                <div className="overflow-x-auto px-4 py-2">
                    <Table>
                        <TableHeader>
                            <TableRow>
                                <TableHead
                                    className="cursor-pointer select-none"
                                    onClick={() => handleSort('passager')}
                                >
                                    Passager
                                    {getSortIcon('passager')}
                                </TableHead>
                                <TableHead
                                    className="cursor-pointer select-none"
                                    onClick={() => handleSort('vol')}
                                >
                                    Vol
                                    {getSortIcon('vol')}
                                </TableHead>
                                <TableHead>Siège</TableHead>
                                <TableHead
                                    className="cursor-pointer select-none"
                                    onClick={() => handleSort('heureCheckIn')}
                                >
                                    Heure Check-in
                                    {getSortIcon('heureCheckIn')}
                                </TableHead>
                                <TableHead>Email</TableHead>
                                <TableHead className="text-right">Actions</TableHead>
                            </TableRow>
                        </TableHeader>
                        <TableBody>
                            {loading ? (
                                <TableRow>
                                    <TableCell colSpan={6}>Chargement…</TableCell>
                                </TableRow>
                            ) : displayedCheckIns.length === 0 ? (
                                <TableRow>
                                    <TableCell colSpan={6} className="text-muted-foreground">
                                        {volFilter !== "ALL" ? "Aucun check-in pour ce vol" : "Aucun check-in enregistré"}
                                    </TableCell>
                                </TableRow>
                            ) : (
                                displayedCheckIns.map((c) => (
                                    <TableRow key={c.id}>
                                        <TableCell className="font-medium">{getPassagerName(c)}</TableCell>
                                        <TableCell>{getVolNumero(c.volId)}</TableCell>
                                        <TableCell className="font-mono font-semibold">{c.numeroSiege}</TableCell>
                                        <TableCell>{fmt.format(new Date(c.heureCheckIn))}</TableCell>
                                        <TableCell className="text-sm text-muted-foreground">
                                            {c.passagerInfo?.email || "—"}
                                        </TableCell>
                                        <TableCell className="text-right">
                                            <DropdownMenu>
                                                <DropdownMenuTrigger asChild>
                                                    <Button size="icon" variant="ghost">
                                                        <MoreHorizontal className="h-4 w-4" />
                                                    </Button>
                                                </DropdownMenuTrigger>
                                                <DropdownMenuContent align="end">
                                                    <DropdownMenuItem onClick={() => startEdit(c)}>
                                                        <Pencil className="mr-2 h-4 w-4" /> Modifier le siège
                                                    </DropdownMenuItem>
                                                    <DropdownMenuItem
                                                        className="text-red-600"
                                                        onClick={() => deleteCheckIn(c.id, getPassagerName(c))}
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
                        <DialogTitle>Nouveau check-in</DialogTitle>
                        <DialogDescription>Enregistrez un passager sur un vol</DialogDescription>
                    </DialogHeader>
                    <div className="grid gap-4 py-2">
                        <div className="space-y-2">
                            <Label>Passager</Label>
                            {passengers.length === 0 ? (
                                <p className="text-sm text-muted-foreground py-2">
                                    Aucun passager disponible
                                </p>
                            ) : (
                                <>
                                    <Select
                                        value={createForm.passagerId}
                                        onValueChange={(v) => setCreateField("passagerId", v)}
                                    >
                                        <SelectTrigger className={validationErrors.passagerId ? "border-red-500" : ""}>
                                            <SelectValue placeholder="Sélectionner un passager" />
                                        </SelectTrigger>
                                        <SelectContent>
                                            {passengers.map((p) => (
                                                <SelectItem key={p.id} value={p.id}>
                                                    {p.prenom} {p.nom} ({p.email})
                                                </SelectItem>
                                            ))}
                                        </SelectContent>
                                    </Select>
                                    {validationErrors.passagerId && (
                                        <p className="text-sm text-red-600">{validationErrors.passagerId}</p>
                                    )}
                                </>
                            )}
                        </div>

                        <div className="space-y-2">
                            <Label>Vol</Label>
                            {availableVols.length === 0 ? (
                                <p className="text-sm text-muted-foreground py-2">
                                    Aucun vol disponible pour le check-in (statut PREVU ou EMBARQUEMENT avec avion assigné requis)
                                </p>
                            ) : (
                                <>
                                    <Select
                                        value={createForm.volId}
                                        onValueChange={(v) => setCreateField("volId", v)}
                                    >
                                        <SelectTrigger className={validationErrors.volId ? "border-red-500" : ""}>
                                            <SelectValue placeholder="Sélectionner un vol" />
                                        </SelectTrigger>
                                        <SelectContent>
                                            {availableVols.map((v) => (
                                                <SelectItem key={v.id} value={v.id}>
                                                    {v.numeroVol} - {v.origine} → {v.destination} ({fmt.format(new Date(v.heureDepart))})
                                                </SelectItem>
                                            ))}
                                        </SelectContent>
                                    </Select>
                                    {validationErrors.volId && (
                                        <p className="text-sm text-red-600">{validationErrors.volId}</p>
                                    )}
                                </>
                            )}
                        </div>

                        <div className="space-y-2">
                            <Label>Numéro de siège</Label>
                            <Input
                                placeholder="12A, 5F (Format: [Numéro][Lettre A-F])"
                                value={createForm.numeroSiege}
                                onChange={(e) => setCreateField("numeroSiege", e.target.value.toUpperCase())}
                                className={validationErrors.numeroSiege ? "border-red-500" : ""}
                                maxLength={4}
                            />
                            <p className="text-xs text-muted-foreground">
                                Format: numéro de rangée suivi d'une lettre (A, B, C, D, E ou F)
                            </p>
                            {validationErrors.numeroSiege && (
                                <p className="text-sm text-red-600">{validationErrors.numeroSiege}</p>
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
                        <Button
                            onClick={createCheckIn}
                            disabled={passengers.length === 0 || availableVols.length === 0}
                        >
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
                        <DialogTitle>Modifier le siège</DialogTitle>
                        <DialogDescription>
                            Passager: {openEdit ? getPassagerName(openEdit) : ""} - Vol: {openEdit ? getVolNumero(openEdit.volId) : ""}
                        </DialogDescription>
                    </DialogHeader>
                    <div className="grid gap-4 py-2">
                        <div className="space-y-2">
                            <Label>Numéro de siège</Label>
                            <Input
                                placeholder="12A, 5F"
                                value={editForm.numeroSiege}
                                onChange={(e) => setEditForm((f) => ({ ...f, numeroSiege: e.target.value.toUpperCase() }))}
                                className={validationErrors.numeroSiege ? "border-red-500" : ""}
                                maxLength={4}
                            />
                            <p className="text-xs text-muted-foreground">
                                Format: numéro de rangée suivi d'une lettre (A, B, C, D, E ou F)
                            </p>
                            {validationErrors.numeroSiege && (
                                <p className="text-sm text-red-600">{validationErrors.numeroSiege}</p>
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
                            <Button onClick={() => updateCheckIn(openEdit.id)}>
                                Enregistrer
                            </Button>
                        )}
                    </DialogFooter>
                </DialogContent>
            </Dialog>
        </div>
    );
}
