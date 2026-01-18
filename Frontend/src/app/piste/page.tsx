"use client";

import { useEffect, useState } from "react";
import { Button } from "@/components/ui/button";
import {
    Dialog,
    DialogContent,
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
import { Badge } from "@/components/ui/badge";
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
    RefreshCw,
    Trash2,
    Construction,
    CheckCircle2,
    XCircle,
    Pencil
} from "lucide-react";

const API_BASE = process.env.NEXT_PUBLIC_API_BASE ?? "";

type Piste = {
    id: string;
    identifiant: string;
    longueurM: number;
    etat: string;
};

type CreatePisteRequest = {
    identifiant: string;
    longueurM: number;
    etat?: string;
};

type UpdatePisteEtatRequest = {
    etat: string;
};

type Notification = {
    type: 'success' | 'error';
    message: string;
} | null;

type ValidationErrors = {
    identifiant?: string;
    longueurM?: string;
    etat?: string;
};

const CREATION_ETATS = ["LIBRE", "MAINTENANCE"] as const;

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

export default function PistePage() {
    const [items, setItems] = useState<Piste[]>([]);
    const [loading, setLoading] = useState(true);
    const [notification, setNotification] = useState<Notification>(null);
    const [onlyDisponibles, setOnlyDisponibles] = useState(false);
    const [openCreate, setOpenCreate] = useState(false);
    const [openEtat, setOpenEtat] = useState<{ id: string; currentEtat: string } | null>(null);
    const [createForm, setCreateForm] = useState<CreatePisteRequest>({
        identifiant: "",
        longueurM: 1000,
        etat: "LIBRE",
    });
    const [etatForm, setEtatForm] = useState<UpdatePisteEtatRequest>({ etat: "LIBRE" });
    const [validationErrors, setValidationErrors] = useState<ValidationErrors>({});

    const showNotification = (type: 'success' | 'error', message: string) => {
        setNotification({ type, message });
        setTimeout(() => setNotification(null), 5000);
    };

    const load = async (only = false) => {
        try {
            setLoading(true);
            const data = await api<Piste[]>(only ? "/api/pistes/disponibles" : "/api/pistes");
            setItems(data);
        } catch (e: any) {
            showNotification('error', e.message);
        }
        setLoading(false);
    };

    useEffect(() => {
        load(onlyDisponibles);
    }, [onlyDisponibles]);

    const badgeForEtat = (etat: string) => {
        const e = etat.toUpperCase();
        if (e === "LIBRE") return <Badge className="bg-green-600 text-white">{etat}</Badge>;
        if (e === "OCCUPEE") return <Badge className="bg-yellow-600 text-white">{etat}</Badge>;
        if (e === "MAINTENANCE") return <Badge className="bg-red-600 text-white">{etat}</Badge>;
        return <Badge variant="secondary">{etat}</Badge>;
    };

    const setCreateField = (k: string, v: any) => {
        setCreateForm((f) => ({ ...f, [k]: v }));
        if (validationErrors[k as keyof ValidationErrors]) {
            setValidationErrors((prev) => ({ ...prev, [k]: undefined }));
        }
    };

    const resetCreateForm = () => {
        setCreateForm({ identifiant: "", longueurM: 1000, etat: "LIBRE" });
        setValidationErrors({});
    };

    const createPiste = async () => {
        try {
            const payload: CreatePisteRequest = {
                identifiant: createForm.identifiant.trim().toUpperCase(),
                longueurM: Number(createForm.longueurM),
                etat: createForm.etat,
            };

            await api("/api/pistes", {
                method: "POST",
                body: JSON.stringify(payload),
            });

            setOpenCreate(false);
            resetCreateForm();
            showNotification('success', 'Piste créée avec succès');
            load(onlyDisponibles);
        } catch (e: any) {
            const errorMessage = e.message.toLowerCase();
            const errors: ValidationErrors = {};

            if (errorMessage.includes('identifiant') || errorMessage.includes('invalide') ||
                errorMessage.includes('exemples')) {
                errors.identifiant = e.message;
            } else if (errorMessage.includes('longueur') || errorMessage.includes('825')) {
                errors.longueurM = e.message;
            } else if (errorMessage.includes('état') || errorMessage.includes('etat') ||
                errorMessage.includes('libre') || errorMessage.includes('maintenance')) {
                errors.etat = e.message;
            } else {
                showNotification('error', e.message);
                return;
            }

            setValidationErrors(errors);
        }
    };

    const openChangeEtat = (p: Piste) => {
        const allowedEtat = p.etat.toUpperCase() === "MAINTENANCE" ? "LIBRE" : "MAINTENANCE";
        setEtatForm({ etat: allowedEtat });
        setOpenEtat({ id: p.id, currentEtat: p.etat });
        setValidationErrors({});
    };

    const updateEtat = async () => {
        if (!openEtat) return;

        try {
            const payload = { etat: etatForm.etat };
            await api(`/api/pistes/${openEtat.id}/etat`, {
                method: "PATCH",
                body: JSON.stringify(payload),
            });

            setOpenEtat(null);
            showNotification('success', 'État de la piste modifié avec succès');
            load(onlyDisponibles);
        } catch (e: any) {
            const errorMessage = e.message.toLowerCase();

            if (errorMessage.includes('transition') || errorMessage.includes('état') ||
                errorMessage.includes('etat') || errorMessage.includes('autorisée')) {
                setValidationErrors({ etat: e.message });
            } else {
                showNotification('error', e.message);
            }
        }
    };

    const deletePiste = async (id: string) => {
        if (!confirm("Supprimer cette piste ?")) return;

        try {
            await fetch(`${API_BASE}/api/pistes/${id}`, { method: "DELETE" });
            showNotification('success', 'Piste supprimée avec succès');
            load(onlyDisponibles);
        } catch (e: any) {
            showNotification('error', e.message);
        }
    };

    const getAvailableEtatForChange = (currentEtat: string): string[] => {
        const upperEtat = currentEtat.toUpperCase();
        if (upperEtat === "MAINTENANCE") return ["LIBRE"];
        if (upperEtat === "LIBRE") return ["MAINTENANCE"];
        return [];
    };

    return (
        <div className="mx-auto max-w-6xl px-4 py-8 space-y-6">
            <div className="flex flex-wrap items-center justify-between gap-3">
                <div>
                    <h1 className="text-2xl font-semibold">Pistes</h1>
                </div>
                <div className="flex flex-wrap items-center gap-2">
                    <Button
                        variant={onlyDisponibles ? "default" : "outline"}
                        onClick={() => setOnlyDisponibles((v) => !v)}
                    >
                        {onlyDisponibles ? (
                            <>
                                <CheckCircle2 className="mr-2 h-4 w-4"/>
                                Pistes libres
                            </>
                        ) : (
                            <>
                                <Construction className="mr-2 h-4 w-4"/>
                                Toutes les pistes
                            </>
                        )}
                    </Button>
                    <Button variant="outline" onClick={() => load(onlyDisponibles)}>
                        <RefreshCw className="mr-2 h-4 w-4"/>
                        Actualiser
                    </Button>
                    <Button onClick={() => {
                        resetCreateForm();
                        setOpenCreate(true);
                    }}>
                        <Plus className="mr-2 h-4 w-4"/>
                        Nouvelle piste
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
                        <CheckCircle2 className="h-4 w-4 text-green-600"/>
                    ) : (
                        <XCircle className="h-4 w-4 text-red-600"/>
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
                                <TableHead>Identifiant</TableHead>
                                <TableHead>Longueur (m)</TableHead>
                                <TableHead>État</TableHead>
                                <TableHead className="text-right">Actions</TableHead>
                            </TableRow>
                        </TableHeader>
                        <TableBody>
                            {loading ? (
                                <TableRow>
                                    <TableCell colSpan={4}>Chargement…</TableCell>
                                </TableRow>
                            ) : items.length === 0 ? (
                                <TableRow>
                                    <TableCell colSpan={4}>Aucune piste</TableCell>
                                </TableRow>
                            ) : (
                                items.map((p) => (
                                    <TableRow key={p.id}>
                                        <TableCell>{p.identifiant}</TableCell>
                                        <TableCell>{p.longueurM}</TableCell>
                                        <TableCell>{badgeForEtat(p.etat)}</TableCell>
                                        <TableCell className="text-right">
                                            <DropdownMenu>
                                                <DropdownMenuTrigger asChild>
                                                    <Button size="icon" variant="ghost">
                                                        <MoreHorizontal className="h-4 w-4"/>
                                                    </Button>
                                                </DropdownMenuTrigger>
                                                <DropdownMenuContent align="end">
                                                    {p.etat.toUpperCase() !== "OCCUPEE" && (
                                                        <DropdownMenuItem onClick={() => openChangeEtat(p)}>
                                                            <Pencil className="mr-2 h-4 w-4"/> Changer l'état
                                                        </DropdownMenuItem>
                                                    )}
                                                    <DropdownMenuItem
                                                        className="text-red-600"
                                                        onClick={() => deletePiste(p.id)}
                                                    >
                                                        <Trash2 className="mr-2 h-4 w-4"/> Supprimer
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
                        <DialogTitle>Nouvelle piste</DialogTitle>
                    </DialogHeader>

                    <div className="grid gap-4 py-2">
                        <div className="space-y-2">
                            <Label>Identifiant</Label>
                            <Input
                                placeholder="09L, 27R, 18, A1, H2"
                                value={createForm.identifiant}
                                onChange={(e) => setCreateField("identifiant", e.target.value.toUpperCase())}
                                className={validationErrors.identifiant ? "border-red-500" : ""}
                            />
                            {validationErrors.identifiant && (
                                <p className="text-sm text-red-600">
                                    {validationErrors.identifiant}
                                </p>
                            )}
                        </div>

                        <div className="space-y-2">
                            <Label>Longueur (mètres)</Label>
                            <Input
                                type="number"
                                min={825}
                                placeholder="825 minimum"
                                value={createForm.longueurM}
                                onChange={(e) => setCreateField("longueurM", e.target.value)}
                                className={validationErrors.longueurM ? "border-red-500" : ""}
                            />
                            {validationErrors.longueurM && (
                                <p className="text-sm text-red-600">
                                    {validationErrors.longueurM}
                                </p>
                            )}
                        </div>

                        <div className="space-y-2">
                            <Label>État</Label>
                            <Select
                                value={createForm.etat}
                                onValueChange={(v) => setCreateField("etat", v)}
                            >
                                <SelectTrigger className={validationErrors.etat ? "border-red-500" : ""}>
                                    <SelectValue/>
                                </SelectTrigger>
                                <SelectContent>
                                    {CREATION_ETATS.map((e) => (
                                        <SelectItem key={e} value={e}>
                                            {e}
                                        </SelectItem>
                                    ))}
                                </SelectContent>
                            </Select>
                            {validationErrors.etat && (
                                <p className="text-sm text-red-600">
                                    {validationErrors.etat}
                                </p>
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
                        <Button onClick={createPiste}>Enregistrer</Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>

            <Dialog open={!!openEtat} onOpenChange={(o) => {
                if (!o) {
                    setOpenEtat(null);
                    setValidationErrors({});
                }
            }}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>Changer l'état de la piste</DialogTitle>
                    </DialogHeader>

                    <div className="grid gap-4 py-2">
                        <div className="space-y-2">
                            <Label>État actuel</Label>
                            <div className="p-2 bg-gray-100 rounded">
                                {openEtat && badgeForEtat(openEtat.currentEtat)}
                            </div>
                        </div>

                        <div className="space-y-2">
                            <Label>Nouvel état</Label>
                            <Select
                                value={etatForm.etat}
                                onValueChange={(v) => setEtatForm({ etat: v })}
                            >
                                <SelectTrigger className={validationErrors.etat ? "border-red-500" : ""}>
                                    <SelectValue/>
                                </SelectTrigger>
                                <SelectContent>
                                    {openEtat && getAvailableEtatForChange(openEtat.currentEtat).map((e) => (
                                        <SelectItem key={e} value={e}>
                                            {e}
                                        </SelectItem>
                                    ))}
                                </SelectContent>
                            </Select>
                            {validationErrors.etat && (
                                <p className="text-sm text-red-600">
                                    {validationErrors.etat}
                                </p>
                            )}
                        </div>
                    </div>

                    <DialogFooter>
                        <Button variant="outline" onClick={() => {
                            setOpenEtat(null);
                            setValidationErrors({});
                        }}>
                            Annuler
                        </Button>
                        <Button onClick={updateEtat}>Mettre à jour</Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>
        </div>
    );
}
