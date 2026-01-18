"use client";

import { useEffect, useState } from "react";
import { Button } from "@/components/ui/button";
import {
    Dialog,
    DialogContent,
    DialogHeader,
    DialogTitle,
    DialogFooter,
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
    TableRow
} from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import { Card } from "@/components/ui/card";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { MoreHorizontal, Plus, Pencil, Trash2, Unlink2, RefreshCw, ArrowUpDown, ArrowUp, ArrowDown, Filter, CheckCircle2, XCircle } from "lucide-react";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger
} from "@/components/ui/dropdown-menu";

type Avion = {
    id: string;
    immatriculation: string;
    type: string;
    capacite: number;
    etat: string;
    hangarId?: string | null;
    hangarIdentifiant?: string | null;
};

type Hangar = {
    id: string;
    identifiant: string;
    capacite: number;
    etat: string;
};

type CreateAvionRequest = {
    immatriculation: string;
    type: string;
    capacite: number;
    etat?: string;
    hangarId?: string | null;
};

type UpdateAvionRequest = {
    type?: string;
    capacite?: number;
    etat?: string;
};

type SortConfig = {
    key: 'immatriculation' | 'type' | 'capacite' | 'hangarIdentifiant' | null;
    direction: 'asc' | 'desc' | null;
};

type Notification = {
    type: 'success' | 'error';
    message: string;
} | null;

type ValidationErrors = {
    immatriculation?: string;
    type?: string;
    capacite?: string;
    etat?: string;
    hangarId?: string;
};

const API_BASE = process.env.NEXT_PUBLIC_API_BASE ?? "";

async function api<T>(path: string, init?: RequestInit): Promise<T> {
    const res = await fetch(`${API_BASE}${path}`, {
        ...init,
        headers: {
            "Content-Type": "application/json",
            ...(init?.headers || {}),
        },
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
    return res.json() as Promise<T>;
}

export default function AvionPage() {
    const [allItems, setAllItems] = useState<Avion[]>([]);
    const [items, setItems] = useState<Avion[]>([]);
    const [hangars, setHangars] = useState<Hangar[]>([]);
    const [loading, setLoading] = useState(true);
    const [notification, setNotification] = useState<Notification>(null);

    const [sortConfig, setSortConfig] = useState<SortConfig>({ key: null, direction: null });
    const [etatFilter, setEtatFilter] = useState<string>("all");

    const [openCreate, setOpenCreate] = useState(false);
    const [openEdit, setOpenEdit] = useState<Avion | null>(null);

    const [form, setForm] = useState({
        immatriculation: "",
        type: "",
        capacite: 1,
        etat: undefined as string | undefined,
        hangarId: null as string | null,
    });

    const [validationErrors, setValidationErrors] = useState<ValidationErrors>({});

    const showNotification = (type: 'success' | 'error', message: string) => {
        setNotification({ type, message });
        setTimeout(() => setNotification(null), 5000);
    };

    const load = async () => {
        setLoading(true);
        try {
            const [avionData, hangarData] = await Promise.all([
                api<Avion[]>("/api/avions"),
                api<Hangar[]>("/api/hangars")
            ]);
            setAllItems(avionData);
            setItems(avionData);
            setHangars(hangarData);
        } catch (e: any) {
            showNotification('error', e.message);
        }
        setLoading(false);
    };

    useEffect(() => {
        load();
    }, []);

    useEffect(() => {
        let filtered = [...allItems];

        if (etatFilter !== "all") {
            filtered = filtered.filter(a => a.etat === etatFilter);
        }

        if (sortConfig.key && sortConfig.direction) {
            filtered.sort((a, b) => {
                let aValue: any;
                let bValue: any;

                if (sortConfig.key === 'capacite') {
                    aValue = a.capacite;
                    bValue = b.capacite;
                } else if (sortConfig.key === 'hangarIdentifiant') {
                    aValue = a.hangarIdentifiant ?? '';
                    bValue = b.hangarIdentifiant ?? '';
                } else if (sortConfig.key === 'immatriculation') {
                    aValue = a.immatriculation;
                    bValue = b.immatriculation;
                } else if (sortConfig.key === 'type') {
                    aValue = a.type;
                    bValue = b.type;
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

        setItems(filtered);
    }, [allItems, sortConfig, etatFilter]);

    const handleSort = (key: 'immatriculation' | 'type' | 'capacite' | 'hangarIdentifiant') => {
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

    const getSortIcon = (key: 'immatriculation' | 'type' | 'capacite' | 'hangarIdentifiant') => {
        if (sortConfig.key !== key || sortConfig.direction === null) {
            return <ArrowUpDown className="ml-2 h-4 w-4 inline-block" />;
        }
        return sortConfig.direction === 'asc'
            ? <ArrowUp className="ml-2 h-4 w-4 inline-block" />
            : <ArrowDown className="ml-2 h-4 w-4 inline-block" />;
    };

    const resetForm = () => {
        setForm({
            immatriculation: "",
            type: "",
            capacite: 1,
            etat: undefined,
            hangarId: null,
        });
        setValidationErrors({});
    };

    const setField = (k: string, v: any) => {
        setForm((f) => ({ ...f, [k]: v }));
        if (validationErrors[k as keyof ValidationErrors]) {
            setValidationErrors((prev) => ({ ...prev, [k]: undefined }));
        }
    };

    const startCreate = () => {
        resetForm();
        setOpenCreate(true);
    };

    const startEdit = (a: Avion) => {
        setForm({
            immatriculation: a.immatriculation,
            type: a.type,
            capacite: a.capacite,
            etat: a.etat,
            hangarId: a.hangarId ?? null,
        });
        setValidationErrors({});
        setOpenEdit(a);
    };

    const createAvion = async () => {
        try {
            const payload: CreateAvionRequest = {
                immatriculation: form.immatriculation.trim().toUpperCase(),
                type: form.type.trim().toUpperCase(),
                capacite: Number(form.capacite),
                etat: form.etat,
                hangarId: form.hangarId,
            };
            await api<Avion>("/api/avions", {
                method: "POST",
                body: JSON.stringify(payload),
            });
            setOpenCreate(false);
            setValidationErrors({});
            showNotification('success', 'Avion créé avec succès');
            load();
        } catch (e: any) {
            const errorMessage = e.message.toLowerCase();
            const errors: ValidationErrors = {};

            if (errorMessage.includes('immatriculation') &&
                (errorMessage.includes('already') || errorMessage.includes('utilisé') || errorMessage.includes('used'))) {
                errors.immatriculation = e.message;
            } else if (errorMessage.includes('immatriculation') || errorMessage.includes('format')) {
                errors.immatriculation = e.message;
            } else if (errorMessage.includes('type') || errorMessage.includes('avion')) {
                errors.type = e.message;
            } else if (errorMessage.includes('capacité') || errorMessage.includes('capacite')) {
                errors.capacite = e.message;
            } else if (errorMessage.includes('état') || errorMessage.includes('etat') ||
                errorMessage.includes('state') || errorMessage.includes('en_vol')) {
                errors.etat = e.message;
            } else if (errorMessage.includes('hangar') || errorMessage.includes('assigné') ||
                errorMessage.includes('assigned') || errorMessage.includes('accept')) {
                errors.hangarId = e.message;
            } else {
                showNotification('error', e.message);
                return;
            }

            setValidationErrors(errors);
        }
    };

    const updateAvion = async (id: string) => {
        try {
            const payload: UpdateAvionRequest = {
                type: form.type.trim().toUpperCase(),
                capacite: Number(form.capacite),
                etat: form.etat,
            };

            const currentHangarId = openEdit?.hangarId;
            const newHangarId = form.hangarId;

            await api<Avion>(`/api/avions/${id}`, {
                method: "PATCH",
                body: JSON.stringify(payload),
            });

            if (currentHangarId !== newHangarId) {
                if (newHangarId) {
                    await api<Avion>(`/api/avions/${id}/assign-hangar/${newHangarId}`, {
                        method: "POST"
                    });
                } else if (currentHangarId && !newHangarId) {
                    await api<Avion>(`/api/avions/${id}/unassign-hangar`, {
                        method: "POST"
                    });
                }
            }

            setOpenEdit(null);
            setValidationErrors({});
            showNotification('success', 'Avion modifié avec succès');
            load();
        } catch (e: any) {
            const errorMessage = e.message.toLowerCase();
            const errors: ValidationErrors = {};

            if (errorMessage.includes('type') || errorMessage.includes('avion')) {
                errors.type = e.message;
            } else if (errorMessage.includes('capacité') || errorMessage.includes('capacite')) {
                errors.capacite = e.message;
            } else if (errorMessage.includes('en_vol') || errorMessage.includes('while') ||
                errorMessage.includes('cannot update')) {
                errors.etat = e.message;
            } else if (errorMessage.includes('état') || errorMessage.includes('etat') ||
                errorMessage.includes('state') || errorMessage.includes('transition')) {
                errors.etat = e.message;
            } else if (errorMessage.includes('hangar') || errorMessage.includes('assigné') ||
                errorMessage.includes('assigned') || errorMessage.includes('disponible') ||
                errorMessage.includes('unassign') || errorMessage.includes('accept')) {
                errors.hangarId = e.message;
            } else {
                showNotification('error', e.message);
                return;
            }

            setValidationErrors(errors);
        }
    };

    const deleteAvion = async (id: string) => {
        if (!confirm("Supprimer cet avion ?")) return;
        try {
            await fetch(`${API_BASE}/api/avions/${id}`, { method: "DELETE" });
            showNotification('success', 'Avion supprimé avec succès');
            load();
        } catch (e: any) {
            showNotification('error', e.message);
        }
    };

    const unassignHangar = async (id: string) => {
        try {
            await api<Avion>(`/api/avions/${id}/unassign-hangar`, { method: "POST" });
            showNotification('success', 'Avion retiré du hangar avec succès');
            load();
        } catch (e: any) {
            showNotification('error', e.message);
        }
    };

    const badgeForEtat = (etat: string) => {
        const u = etat.toUpperCase();
        if (u === "DISPONIBLE") return <Badge className="bg-green-600 text-white">{etat}</Badge>;
        if (u === "EN_VOL") return <Badge className="bg-blue-600 text-white">{etat}</Badge>;
        if (u === "MAINTENANCE") return <Badge className="bg-red-600 text-white">{etat}</Badge>;
        return <Badge>{etat}</Badge>;
    };

    const availableHangars = hangars.filter(h => h.etat.toUpperCase() === "DISPONIBLE");

    return (
        <div className="mx-auto max-w-6xl px-4 py-8 space-y-6">
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-2xl font-semibold">Avion</h1>
                </div>
                <div className="flex items-center gap-2">
                    <Select value={etatFilter} onValueChange={setEtatFilter}>
                        <SelectTrigger className="w-[170px]">
                            <Filter className="h-4 w-4" />
                            <SelectValue placeholder="Filtrer par état" />
                        </SelectTrigger>
                        <SelectContent>
                            <SelectItem value="all">Tous les états</SelectItem>
                            <SelectItem value="DISPONIBLE">DISPONIBLE</SelectItem>
                            <SelectItem value="EN_VOL">EN_VOL</SelectItem>
                            <SelectItem value="MAINTENANCE">MAINTENANCE</SelectItem>
                        </SelectContent>
                    </Select>
                    <Button variant="outline" onClick={load}>
                        <RefreshCw className="mr-2 h-4 w-4" />
                        Actualiser
                    </Button>
                    <Button onClick={startCreate}>
                        <Plus className="mr-2 h-4 w-4" />
                        Nouvel avion
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
                                    onClick={() => handleSort('immatriculation')}
                                >
                                    Immatriculation
                                    {getSortIcon('immatriculation')}
                                </TableHead>
                                <TableHead
                                    className="cursor-pointer select-none"
                                    onClick={() => handleSort('type')}
                                >
                                    Type
                                    {getSortIcon('type')}
                                </TableHead>
                                <TableHead
                                    className="cursor-pointer select-none"
                                    onClick={() => handleSort('capacite')}
                                >
                                    Capacité
                                    {getSortIcon('capacite')}
                                </TableHead>
                                <TableHead>État</TableHead>
                                <TableHead
                                    className="cursor-pointer select-none"
                                    onClick={() => handleSort('hangarIdentifiant')}
                                >
                                    Hangar
                                    {getSortIcon('hangarIdentifiant')}
                                </TableHead>
                                <TableHead className="w-20 text-right">Actions</TableHead>
                            </TableRow>
                        </TableHeader>
                        <TableBody>
                            {loading ? (
                                <TableRow>
                                    <TableCell colSpan={6}>Chargement…</TableCell>
                                </TableRow>
                            ) : items.length === 0 ? (
                                <TableRow>
                                    <TableCell colSpan={6}>Aucun avion.</TableCell>
                                </TableRow>
                            ) : (
                                items.map((a) => (
                                    <TableRow key={a.id}>
                                        <TableCell>{a.immatriculation}</TableCell>
                                        <TableCell>{a.type}</TableCell>
                                        <TableCell>{a.capacite}</TableCell>
                                        <TableCell>{badgeForEtat(a.etat)}</TableCell>
                                        <TableCell>{a.hangarIdentifiant ?? "—"}</TableCell>
                                        <TableCell className="text-right">
                                            <DropdownMenu>
                                                <DropdownMenuTrigger asChild>
                                                    <Button size="icon" variant="ghost">
                                                        <MoreHorizontal className="h-4 w-4" />
                                                    </Button>
                                                </DropdownMenuTrigger>
                                                <DropdownMenuContent align="end">
                                                    <DropdownMenuItem onClick={() => startEdit(a)}>
                                                        <Pencil className="mr-2 h-4 w-4" /> Modifier
                                                    </DropdownMenuItem>
                                                    {a.hangarId && (
                                                        <DropdownMenuItem onClick={() => unassignHangar(a.id)}>
                                                            <Unlink2 className="mr-2 h-4 w-4" /> Retirer du hangar
                                                        </DropdownMenuItem>
                                                    )}
                                                    <DropdownMenuItem
                                                        className="text-red-600"
                                                        onClick={() => deleteAvion(a.id)}
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

            <Dialog open={openCreate} onOpenChange={(o) => {
                if (!o) {
                    setOpenCreate(false);
                    setValidationErrors({});
                }
            }}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>Nouvel avion</DialogTitle>
                    </DialogHeader>

                    <div className="grid gap-4 py-2">
                        <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
                            <div className="space-y-2">
                                <Label>Immatriculation</Label>
                                <Input
                                    placeholder="F-BZHE, HB-JCA"
                                    value={form.immatriculation}
                                    onChange={(e) => setField("immatriculation", e.target.value.toUpperCase())}
                                    className={validationErrors.immatriculation ? "border-red-500" : ""}
                                />
                                {validationErrors.immatriculation && (
                                    <p className="text-sm text-red-600">
                                        {validationErrors.immatriculation}
                                    </p>
                                )}
                            </div>
                            <div className="space-y-2">
                                <Label>Type</Label>
                                <Input
                                    placeholder="A320, B737"
                                    value={form.type}
                                    onChange={(e) => setField("type", e.target.value.toUpperCase())}
                                    className={validationErrors.type ? "border-red-500" : ""}
                                />
                                {validationErrors.type && (
                                    <p className="text-sm text-red-600">
                                        {validationErrors.type}
                                    </p>
                                )}
                            </div>
                        </div>

                        <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
                            <div className="space-y-2">
                                <Label>Capacité</Label>
                                <Input
                                    type="number"
                                    min={1}
                                    placeholder="180"
                                    value={form.capacite}
                                    onChange={(e) => setField("capacite", e.target.value)}
                                    className={validationErrors.capacite ? "border-red-500" : ""}
                                />
                                {validationErrors.capacite && (
                                    <p className="text-sm text-red-600">
                                        {validationErrors.capacite}
                                    </p>
                                )}
                            </div>
                            <div className="space-y-2">
                                <Label>État</Label>
                                <Select
                                    value={form.etat ?? ""}
                                    onValueChange={(v) => setField("etat", v || undefined)}
                                >
                                    <SelectTrigger
                                        className={validationErrors.etat ? "border-red-500 w-full" : "w-full"}
                                    >
                                        <SelectValue placeholder="Sélectionner un état" />
                                    </SelectTrigger>
                                    <SelectContent>
                                        <SelectItem value="DISPONIBLE">DISPONIBLE</SelectItem>
                                        <SelectItem value="MAINTENANCE">MAINTENANCE</SelectItem>
                                    </SelectContent>
                                </Select>
                                {validationErrors.etat && (
                                    <p className="text-sm text-red-600">
                                        {validationErrors.etat}
                                    </p>
                                )}
                            </div>
                        </div>

                        <div className="space-y-2">
                            <Label>Hangar</Label>
                            {availableHangars.length === 0 ? (
                                <p className="text-sm text-muted-foreground py-2">
                                    Aucun hangar disponible
                                </p>
                            ) : (
                                <>
                                    <Select
                                        value={form.hangarId ?? "none"}
                                        onValueChange={(v) => setField("hangarId", v === "none" ? null : v)}
                                    >
                                        <SelectTrigger
                                            className={validationErrors.hangarId ? "border-red-500 w-full" : "w-full"}
                                        >
                                            <SelectValue placeholder="Sélectionner un hangar (optionnel)" />
                                        </SelectTrigger>
                                        <SelectContent>
                                            <SelectItem value="none">Non assigné</SelectItem>
                                            {availableHangars.map((h) => (
                                                <SelectItem key={h.id} value={h.id}>
                                                    {h.identifiant}
                                                </SelectItem>
                                            ))}
                                        </SelectContent>
                                    </Select>
                                    {validationErrors.hangarId && (
                                        <p className="text-sm text-red-600">
                                            {validationErrors.hangarId}
                                        </p>
                                    )}
                                </>
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
                        <Button onClick={createAvion}>Enregistrer</Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>

            <Dialog open={!!openEdit} onOpenChange={(open) => {
                if (!open) {
                    setOpenEdit(null);
                    setValidationErrors({});
                }
            }}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>Modifier l'avion</DialogTitle>
                    </DialogHeader>

                    <div className="grid gap-4 py-2">
                        <div className="space-y-2">
                            <Label>Immatriculation</Label>
                            <Input value={form.immatriculation} disabled />
                        </div>

                        <div className="space-y-2">
                            <Label>Type</Label>
                            <Input
                                value={form.type}
                                onChange={(e) => setField("type", e.target.value.toUpperCase())}
                                className={validationErrors.type ? "border-red-500" : ""}
                            />
                            {validationErrors.type && (
                                <p className="text-sm text-red-600">
                                    {validationErrors.type}
                                </p>
                            )}
                        </div>

                        <div className="space-y-2">
                            <Label>Capacité</Label>
                            <Input
                                type="number"
                                min={1}
                                value={form.capacite}
                                onChange={(e) => setField("capacite", e.target.value)}
                                className={validationErrors.capacite ? "border-red-500" : ""}
                            />
                            {validationErrors.capacite && (
                                <p className="text-sm text-red-600">
                                    {validationErrors.capacite}
                                </p>
                            )}
                        </div>

                        <div className="space-y-2">
                            <Label>État</Label>
                            <Select
                                value={form.etat ?? ""}
                                onValueChange={(v) => setField("etat", v)}
                            >
                                <SelectTrigger
                                    className={validationErrors.etat ? "border-red-500 w-full" : "w-full"}
                                >
                                    <SelectValue />
                                </SelectTrigger>
                                <SelectContent>
                                    <SelectItem value="DISPONIBLE">DISPONIBLE</SelectItem>
                                    <SelectItem value="EN_VOL">EN_VOL</SelectItem>
                                    <SelectItem value="MAINTENANCE">MAINTENANCE</SelectItem>
                                </SelectContent>
                            </Select>
                            {validationErrors.etat && (
                                <p className="text-sm text-red-600">
                                    {validationErrors.etat}
                                </p>
                            )}
                        </div>

                        <div className="space-y-2">
                            <Label>Hangar</Label>
                            {availableHangars.length === 0 ? (
                                <p className="text-sm text-muted-foreground py-2">
                                    Aucun hangar disponible
                                </p>
                            ) : (
                                <>
                                    <Select
                                        value={form.hangarId ?? "none"}
                                        onValueChange={(v) => setField("hangarId", v === "none" ? null : v)}
                                    >
                                        <SelectTrigger
                                            className={validationErrors.hangarId ? "border-red-500 w-full" : "w-full"}
                                        >
                                            <SelectValue />
                                        </SelectTrigger>
                                        <SelectContent>
                                            <SelectItem value="none">Non assigné</SelectItem>
                                            {availableHangars.map((h) => (
                                                <SelectItem key={h.id} value={h.id}>
                                                    {h.identifiant}
                                                </SelectItem>
                                            ))}
                                        </SelectContent>
                                    </Select>
                                    {validationErrors.hangarId && (
                                        <p className="text-sm text-red-600">
                                            {validationErrors.hangarId}
                                        </p>
                                    )}
                                </>
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
                        <Button onClick={() => openEdit && updateAvion(openEdit.id)}>Enregistrer</Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>
        </div>
    );
}
