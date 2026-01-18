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
    Pencil,
    Trash2,
    RefreshCw,
    Plane,
    Link2,
    Unlink2,
    Flag,
    CheckCircle2,
    XCircle,
    ArrowUpDown,
    ArrowUp,
    ArrowDown,
} from "lucide-react";

const API_BASE = process.env.NEXT_PUBLIC_API_BASE ?? "";
const AIRPORT_CODE = "ATL";

const VOL_ETATS = [
    "PREVU",
    "EN_ATTENTE",
    "EMBARQUEMENT",
    "DECOLLE",
    "EN_VOL",
    "ARRIVE",
    "TERMINE",
    "ANNULE"
] as const;

type Vol = {
    id: string;
    numeroVol: string;
    origine: string;
    destination: string;
    heureDepart: string;
    heureArrivee: string;
    etat: string;
    avionId?: string | null;
    avionImmatriculation?: string | null;
    pisteId?: string | null;
    createdAt?: string | null;
    updatedAt?: string | null;
};

type Avion = {
    id: string;
    immatriculation: string;
    type: string;
    capacite: number;
    etat: string;
};

type CreateVolPayload = {
    numeroVol: string;
    origine: string;
    destination: string;
    heureDepart: string;
    heureArrivee: string;
    avionId: string;
};

type UpdateVolPayload = {
    numeroVol: string;
    origine: string;
    destination: string;
    heureDepart: string;
    heureArrivee: string;
    avionId?: string | null;
};

type Notification = {
    type: 'success' | 'error';
    message: string;
} | null;

type ValidationErrors = {
    numeroVol?: string;
    origine?: string;
    destination?: string;
    heureDepart?: string;
    heureArrivee?: string;
    avionId?: string;
    etat?: string;
};

type SortConfig = {
    key: 'numeroVol' | 'heureDepart' | 'heureArrivee' | 'avionImmatriculation' | null;
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

async function loadHistory(volId: string) {
    return api<{ id: string | null; etat: string; changedAt: string }[]>(
        `/api/vols/${volId}/history`
    );
}

const fmt = new Intl.DateTimeFormat("fr-FR", {
    dateStyle: "short",
    timeStyle: "short",
});

function toLocalDateTimeInputValue(isoLike: string) {
    return isoLike?.slice(0, 16) ?? "";
}

function fromInputToLocalDateTime(value: string) {
    if (!value) return value;
    return value.length === 16 ? `${value}:00` : value;
}

export default function VolPage() {
    const [allVols, setAllVols] = useState<Vol[]>([]);
    const [displayedVols, setDisplayedVols] = useState<Vol[]>([]);
    const [avions, setAvions] = useState<Avion[]>([]);
    const [loading, setLoading] = useState(true);
    const [notification, setNotification] = useState<Notification>(null);
    const [viewMode, setViewMode] = useState<"ALL" | "SORTANT" | "ENTRANT">("ALL");
    const [etatFilter, setEtatFilter] = useState<string>("ALL");
    const [sortConfig, setSortConfig] = useState<SortConfig>({ key: null, direction: null });

    const [openCreate, setOpenCreate] = useState(false);
    const [openEdit, setOpenEdit] = useState<Vol | null>(null);
    const [openEtat, setOpenEtat] = useState<{ id: string; currentEtat: string } | null>(null);
    const [openAssign, setOpenAssign] = useState<{ id: string; currentAvionId?: string | null } | null>(null);
    const [openHistory, setOpenHistory] = useState<{ id: string } | null>(null);
    const [historyList, setHistoryList] = useState<
        { id: string | null; etat: string; changedAt: string }[]
    >([]);

    const [createForm, setCreateForm] = useState<CreateVolPayload>({
        numeroVol: "",
        origine: "",
        destination: "",
        heureDepart: "",
        heureArrivee: "",
        avionId: "",
    });

    const [editForm, setEditForm] = useState<UpdateVolPayload>({
        numeroVol: "",
        origine: "",
        destination: "",
        heureDepart: "",
        heureArrivee: "",
        avionId: undefined,
    });

    const [etatForm, setEtatForm] = useState<string>("PREVU");
    const [assignForm, setAssignForm] = useState<string>("");
    const [validationErrors, setValidationErrors] = useState<ValidationErrors>({});

    const showNotification = (type: 'success' | 'error', message: string) => {
        setNotification({ type, message });
        setTimeout(() => setNotification(null), 5000);
    };

    const load = async () => {
        try {
            setLoading(true);
            const [volsData, avionsData] = await Promise.all([
                api<Vol[]>("/api/vols"),
                api<Avion[]>("/api/avions")
            ]);

            setAvions(avionsData);

            const volsWithImmatriculation = volsData.map(v => ({
                ...v,
                avionImmatriculation: v.avionId ? avionsData.find(a => a.id === v.avionId)?.immatriculation : null
            }));

            if (viewMode === "SORTANT") {
                setAllVols(volsWithImmatriculation.filter(v => v.origine.toUpperCase() === AIRPORT_CODE));
            } else if (viewMode === "ENTRANT") {
                try {
                    const extern = await api<any[]>(`http://129.88.210.74:8080/api/volExterieurs/${AIRPORT_CODE}`);
                    const externMapped: Vol[] = extern.map(e => ({
                        id: `ext-${e.numeroVol}`,
                        numeroVol: e.numeroVol,
                        origine: e.origine,
                        destination: e.destination,
                        heureDepart: e.heureDepart,
                        heureArrivee: e.heureArrivee,
                        etat: e.etat,
                        avionId: null,
                        avionImmatriculation: null,
                    }));
                    const incoming = [
                        ...volsWithImmatriculation.filter(v => v.destination.toUpperCase() === AIRPORT_CODE),
                        ...externMapped
                    ];
                    setAllVols(incoming);
                } catch {
                    setAllVols(volsWithImmatriculation.filter(v => v.destination.toUpperCase() === AIRPORT_CODE));
                }
            } else {
                setAllVols(volsWithImmatriculation);
            }
        } catch (e: any) {
            showNotification('error', e.message);
        }
        setLoading(false);
    };

    useEffect(() => {
        load();
    }, [viewMode]);

    useEffect(() => {
        let filtered = [...allVols];

        if (etatFilter !== "ALL") {
            filtered = filtered.filter(v => v.etat.toUpperCase() === etatFilter);
        }

        if (sortConfig.key && sortConfig.direction) {
            filtered.sort((a, b) => {
                let aValue: any;
                let bValue: any;

                if (sortConfig.key === 'numeroVol') {
                    aValue = a.numeroVol;
                    bValue = b.numeroVol;
                } else if (sortConfig.key === 'heureDepart') {
                    aValue = new Date(a.heureDepart).getTime();
                    bValue = new Date(b.heureDepart).getTime();
                } else if (sortConfig.key === 'heureArrivee') {
                    aValue = new Date(a.heureArrivee).getTime();
                    bValue = new Date(b.heureArrivee).getTime();
                } else if (sortConfig.key === 'avionImmatriculation') {
                    aValue = a.avionImmatriculation ?? '';
                    bValue = b.avionImmatriculation ?? '';
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

        setDisplayedVols(filtered);
    }, [allVols, sortConfig, etatFilter]);

    const handleSort = (key: 'numeroVol' | 'heureDepart' | 'heureArrivee' | 'avionImmatriculation') => {
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

    const getSortIcon = (key: 'numeroVol' | 'heureDepart' | 'heureArrivee' | 'avionImmatriculation') => {
        if (sortConfig.key !== key || sortConfig.direction === null) {
            return <ArrowUpDown className="ml-2 h-4 w-4 inline-block" />;
        }
        return sortConfig.direction === 'asc'
            ? <ArrowUp className="ml-2 h-4 w-4 inline-block" />
            : <ArrowDown className="ml-2 h-4 w-4 inline-block" />;
    };

    const availableAvions = avions.filter(a => a.etat === "DISPONIBLE");

    const getAvionImmatriculation = (avionId: string | null | undefined): string => {
        if (!avionId) return "—";
        const avion = avions.find(a => a.id === avionId);
        return avion?.immatriculation || avionId.slice(0, 8) + "...";
    };

    const badgeForEtat = (etat: string) => {
        const e = etat.toUpperCase();
        if (e === "PREVU") return <Badge className="bg-blue-600 text-white">{etat}</Badge>;
        if (e === "EN_ATTENTE") return <Badge className="bg-yellow-600 text-white">{etat}</Badge>;
        if (e === "EMBARQUEMENT") return <Badge className="bg-purple-600 text-white">{etat}</Badge>;
        if (e === "DECOLLE") return <Badge className="bg-orange-600 text-white">{etat}</Badge>;
        if (e === "EN_VOL") return <Badge className="bg-green-600 text-white">{etat}</Badge>;
        if (e === "ARRIVE") return <Badge className="bg-gray-600 text-white">{etat}</Badge>;
        if (e === "TERMINE") return <Badge className="bg-slate-600 text-white">{etat}</Badge>;
        if (e === "ANNULE") return <Badge className="bg-red-600 text-white">{etat}</Badge>;
        return <Badge variant="secondary">{etat}</Badge>;
    };

    const setCreateField = (k: string, v: any) => {
        setCreateForm((f) => ({ ...f, [k]: v }));
        if (validationErrors[k as keyof ValidationErrors]) {
            setValidationErrors((prev) => ({ ...prev, [k]: undefined }));
        }
    };

    const resetCreateForm = () => {
        setCreateForm({
            numeroVol: "",
            origine: "",
            destination: "",
            heureDepart: "",
            heureArrivee: "",
            avionId: "",
        });
        setValidationErrors({});
    };

    const createVol = async () => {
        try {
            const payload: CreateVolPayload = {
                numeroVol: createForm.numeroVol.trim().toUpperCase(),
                origine: createForm.origine.trim().toUpperCase(),
                destination: createForm.destination.trim().toUpperCase(),
                heureDepart: fromInputToLocalDateTime(createForm.heureDepart),
                heureArrivee: fromInputToLocalDateTime(createForm.heureArrivee),
                avionId: createForm.avionId,
            };

            await api<Vol>("/api/vols", { method: "POST", body: JSON.stringify(payload) });
            setOpenCreate(false);
            resetCreateForm();
            showNotification('success', 'Vol créé avec succès');
            await load();
        } catch (e: any) {
            const errorMessage = e.message.toLowerCase();
            const errors: ValidationErrors = {};

            if (errorMessage.includes('numéro') || errorMessage.includes('numero') ||
                errorMessage.includes('af') || errorMessage.includes('ba')) {
                errors.numeroVol = e.message;
            } else if (errorMessage.includes('origine') || errorMessage.includes('iata') && errorMessage.includes('origine')) {
                errors.origine = e.message;
            } else if (errorMessage.includes('destination') || errorMessage.includes('iata') && errorMessage.includes('destination')) {
                errors.destination = e.message;
            } else if (errorMessage.includes('départ') || errorMessage.includes('depart') || errorMessage.includes('futur') || errorMessage.includes('2h')) {
                errors.heureDepart = e.message;
            } else if (errorMessage.includes('arrivée') || errorMessage.includes('arrivee') || errorMessage.includes('durée') || errorMessage.includes('duree') || errorMessage.includes('minutes')) {
                errors.heureArrivee = e.message;
            } else if (errorMessage.includes('avion') || errorMessage.includes('assigné') || errorMessage.includes('assigne')) {
                errors.avionId = e.message;
            } else {
                showNotification('error', e.message);
                return;
            }

            setValidationErrors(errors);
        }
    };

    const startEdit = (v: Vol) => {
        setOpenEdit(v);
        setEditForm({
            numeroVol: v.numeroVol,
            origine: v.origine,
            destination: v.destination,
            heureDepart: toLocalDateTimeInputValue(v.heureDepart),
            heureArrivee: toLocalDateTimeInputValue(v.heureArrivee),
            avionId: v.avionId,
        });
        setValidationErrors({});
    };

    const updateVol = async (id: string) => {
        try {
            const payload: UpdateVolPayload = {
                numeroVol: editForm.numeroVol.trim().toUpperCase(),
                origine: editForm.origine.trim().toUpperCase(),
                destination: editForm.destination.trim().toUpperCase(),
                heureDepart: fromInputToLocalDateTime(editForm.heureDepart),
                heureArrivee: fromInputToLocalDateTime(editForm.heureArrivee),
                avionId: editForm.avionId,
            };

            await api<Vol>(`/api/vols/${id}`, { method: "PATCH", body: JSON.stringify(payload) });
            setOpenEdit(null);
            setValidationErrors({});
            showNotification('success', 'Vol modifié avec succès');
            await load();
        } catch (e: any) {
            const errorMessage = e.message.toLowerCase();
            const errors: ValidationErrors = {};

            if (errorMessage.includes('numéro') || errorMessage.includes('numero')) {
                errors.numeroVol = e.message;
            } else if (errorMessage.includes('origine')) {
                errors.origine = e.message;
            } else if (errorMessage.includes('destination')) {
                errors.destination = e.message;
            } else if (errorMessage.includes('départ') || errorMessage.includes('depart')) {
                errors.heureDepart = e.message;
            } else if (errorMessage.includes('arrivée') || errorMessage.includes('arrivee') || errorMessage.includes('durée')) {
                errors.heureArrivee = e.message;
            } else {
                showNotification('error', e.message);
                return;
            }

            setValidationErrors(errors);
        }
    };

    const deleteVol = async (id: string) => {
        if (!confirm("Supprimer ce vol ?")) return;
        try {
            await fetch(`${API_BASE}/api/vols/${id}`, { method: "DELETE" });
            showNotification('success', 'Vol supprimé avec succès');
            await load();
        } catch (e: any) {
            showNotification('error', e.message);
        }
    };

    const changeEtat = async () => {
        if (!openEtat) return;
        try {
            await fetch(`${API_BASE}/api/vols/${openEtat.id}/etat`, {
                method: "PATCH",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(etatForm),
            }).then((r) => {
                if (!r.ok) throw new Error("Erreur mise à jour état");
            });
            setOpenEtat(null);
            showNotification('success', 'État du vol modifié avec succès');
            await load();
        } catch (e: any) {
            const errorMessage = e.message.toLowerCase();
            if (errorMessage.includes('transition') || errorMessage.includes('autorisée')) {
                setValidationErrors({ etat: e.message });
            } else {
                showNotification('error', e.message);
            }
        }
    };

    const assignAvion = async () => {
        if (!openAssign || !assignForm) return;
        try {
            await api<Vol>(`/api/vols/${openAssign.id}/assign-avion/${assignForm}`, { method: "POST" });
            setOpenAssign(null);
            setAssignForm("");
            showNotification('success', 'Avion assigné avec succès');
            await load();
        } catch (e: any) {
            showNotification('error', e.message);
        }
    };

    const unassignAvion = async (id: string) => {
        try {
            await api<Vol>(`/api/vols/${id}/unassign-avion`, { method: "POST" });
            showNotification('success', 'Avion retiré avec succès');
            await load();
        } catch (e: any) {
            showNotification('error', e.message);
        }
    };

    const colorForEtat = (etat: string) => {
        const e = etat.toUpperCase();
        if (e === "PREVU") return "bg-blue-600";
        if (e === "EN_ATTENTE") return "bg-yellow-600";
        if (e === "EMBARQUEMENT") return "bg-purple-600";
        if (e === "DECOLLE") return "bg-orange-600";
        if (e === "EN_VOL") return "bg-green-600";
        if (e === "ARRIVE") return "bg-gray-600";
        if (e === "TERMINE") return "bg-slate-600";
        if (e === "ANNULE") return "bg-red-600";
        return "bg-slate-400";
    };

    const isExternal = (volId: string) => volId.startsWith("ext-");

    return (
        <div className="mx-auto max-w-7xl px-4 py-8 space-y-6">
            <div className="flex flex-wrap items-center justify-between gap-3">
                <div>
                    <h1 className="text-2xl font-semibold">Vols</h1>
                    <p className="text-muted-foreground">Planification, suivi et affectation avion</p>
                </div>

                <div className="flex flex-wrap items-center gap-2">
                    <Button
                        variant={viewMode === "ALL" ? "default" : "outline"}
                        onClick={() => setViewMode("ALL")}
                    >
                        Tous
                    </Button>
                    <Button
                        variant={viewMode === "SORTANT" ? "default" : "outline"}
                        onClick={() => setViewMode("SORTANT")}
                    >
                        Sortants
                    </Button>
                    <Button
                        variant={viewMode === "ENTRANT" ? "default" : "outline"}
                        onClick={() => setViewMode("ENTRANT")}
                    >
                        Entrants
                    </Button>

                    <Select value={etatFilter} onValueChange={setEtatFilter}>
                        <SelectTrigger className="w-[170px]">
                            <SelectValue placeholder="Filtrer par état" />
                        </SelectTrigger>
                        <SelectContent>
                            <SelectItem value="ALL">Tous les états</SelectItem>
                            {VOL_ETATS.map((e) => (
                                <SelectItem key={e} value={e}>{e}</SelectItem>
                            ))}
                        </SelectContent>
                    </Select>

                    <Button variant="outline" onClick={load}>
                        <RefreshCw className="mr-2 h-4 w-4" />
                        Actualiser
                    </Button>

                    <Button onClick={() => { resetCreateForm(); setOpenCreate(true); }}>
                        <Plus className="mr-2 h-4 w-4" />
                        Nouveau vol
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
                                    onClick={() => handleSort('numeroVol')}
                                >
                                    Numéro
                                    {getSortIcon('numeroVol')}
                                </TableHead>
                                <TableHead>Origine → Destination</TableHead>
                                <TableHead
                                    className="cursor-pointer select-none"
                                    onClick={() => handleSort('heureDepart')}
                                >
                                    Départ
                                    {getSortIcon('heureDepart')}
                                </TableHead>
                                <TableHead
                                    className="cursor-pointer select-none"
                                    onClick={() => handleSort('heureArrivee')}
                                >
                                    Arrivée
                                    {getSortIcon('heureArrivee')}
                                </TableHead>
                                <TableHead>État</TableHead>
                                <TableHead
                                    className="cursor-pointer select-none"
                                    onClick={() => handleSort('avionImmatriculation')}
                                >
                                    Avion
                                    {getSortIcon('avionImmatriculation')}
                                </TableHead>
                                <TableHead className="text-right">Actions</TableHead>
                            </TableRow>
                        </TableHeader>
                        <TableBody>
                            {loading ? (
                                <TableRow>
                                    <TableCell colSpan={7}>Chargement…</TableCell>
                                </TableRow>
                            ) : displayedVols.length === 0 ? (
                                <TableRow>
                                    <TableCell colSpan={7} className="text-muted-foreground">
                                        Aucun vol
                                    </TableCell>
                                </TableRow>
                            ) : (
                                displayedVols.map((v) => (
                                    <TableRow key={v.id}>
                                        <TableCell className="font-medium">{v.numeroVol}</TableCell>
                                        <TableCell>{v.origine} → {v.destination}</TableCell>
                                        <TableCell>{fmt.format(new Date(v.heureDepart))}</TableCell>
                                        <TableCell>{fmt.format(new Date(v.heureArrivee))}</TableCell>
                                        <TableCell>{badgeForEtat(v.etat)}</TableCell>
                                        <TableCell className="font-medium">
                                            {v.avionImmatriculation || "—"}
                                        </TableCell>
                                        <TableCell className="text-right">
                                            {isExternal(v.id) ? (
                                                <span className="text-xs text-muted-foreground">Externe</span>
                                            ) : (
                                                <DropdownMenu>
                                                    <DropdownMenuTrigger asChild>
                                                        <Button size="icon" variant="ghost">
                                                            <MoreHorizontal className="h-4 w-4" />
                                                        </Button>
                                                    </DropdownMenuTrigger>
                                                    <DropdownMenuContent align="end">
                                                        <DropdownMenuItem onClick={() => {
                                                            setEtatForm(v.etat);
                                                            setOpenEtat({ id: v.id, currentEtat: v.etat });
                                                            setValidationErrors({});
                                                        }}>
                                                            <Flag className="mr-2 h-4 w-4" /> Changer l'état
                                                        </DropdownMenuItem>
                                                        <DropdownMenuItem onClick={() => startEdit(v)}>
                                                            <Pencil className="mr-2 h-4 w-4" /> Modifier
                                                        </DropdownMenuItem>
                                                        {v.avionId ? (
                                                            <DropdownMenuItem onClick={() => unassignAvion(v.id)}>
                                                                <Unlink2 className="mr-2 h-4 w-4" /> Retirer l'avion
                                                            </DropdownMenuItem>
                                                        ) : (
                                                            <DropdownMenuItem onClick={() => {
                                                                setAssignForm("");
                                                                setOpenAssign({ id: v.id, currentAvionId: v.avionId });
                                                            }}>
                                                                <Link2 className="mr-2 h-4 w-4" /> Assigner un avion
                                                            </DropdownMenuItem>
                                                        )}
                                                        <DropdownMenuItem
                                                            onClick={async () => {
                                                                try {
                                                                    const data = await loadHistory(v.id);
                                                                    setHistoryList(data);
                                                                    setOpenHistory({ id: v.id });
                                                                } catch (e: any) {
                                                                    showNotification('error', 'Erreur chargement historique');
                                                                }
                                                            }}
                                                        >
                                                            <Plane className="mr-2 h-4 w-4" /> Historique
                                                        </DropdownMenuItem>
                                                        <DropdownMenuItem
                                                            className="text-red-600"
                                                            onClick={() => deleteVol(v.id)}
                                                        >
                                                            <Trash2 className="mr-2 h-4 w-4" /> Supprimer
                                                        </DropdownMenuItem>
                                                    </DropdownMenuContent>
                                                </DropdownMenu>
                                            )}
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
                        <DialogTitle>Nouveau vol</DialogTitle>
                        <DialogDescription>Renseignez les informations du vol</DialogDescription>
                    </DialogHeader>
                    <div className="grid gap-4 py-2">
                        <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
                            <div className="space-y-2">
                                <Label>Numéro de vol</Label>
                                <Input
                                    placeholder="AF1234, BA456"
                                    value={createForm.numeroVol}
                                    onChange={(e) => setCreateField("numeroVol", e.target.value.toUpperCase())}
                                    className={validationErrors.numeroVol ? "border-red-500" : ""}
                                />
                                {validationErrors.numeroVol && (
                                    <p className="text-sm text-red-600">{validationErrors.numeroVol}</p>
                                )}
                            </div>
                            <div className="space-y-2">
                                <Label>Origine</Label>
                                <Input
                                    placeholder="CDG, ATL, JFK"
                                    value={createForm.origine}
                                    onChange={(e) => setCreateField("origine", e.target.value.toUpperCase())}
                                    className={validationErrors.origine ? "border-red-500" : ""}
                                    maxLength={3}
                                />
                                {validationErrors.origine && (
                                    <p className="text-sm text-red-600">{validationErrors.origine}</p>
                                )}
                            </div>
                            <div className="space-y-2">
                                <Label>Destination</Label>
                                <Input
                                    placeholder="CDG, ATL, JFK"
                                    value={createForm.destination}
                                    onChange={(e) => setCreateField("destination", e.target.value.toUpperCase())}
                                    className={validationErrors.destination ? "border-red-500" : ""}
                                    maxLength={3}
                                />
                                {validationErrors.destination && (
                                    <p className="text-sm text-red-600">{validationErrors.destination}</p>
                                )}
                            </div>
                        </div>

                        <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
                            <div className="space-y-2">
                                <Label>Heure de départ</Label>
                                <Input
                                    type="datetime-local"
                                    value={createForm.heureDepart}
                                    onChange={(e) => setCreateField("heureDepart", e.target.value)}
                                    className={validationErrors.heureDepart ? "border-red-500" : ""}
                                />
                                {validationErrors.heureDepart && (
                                    <p className="text-sm text-red-600">{validationErrors.heureDepart}</p>
                                )}
                            </div>
                            <div className="space-y-2">
                                <Label>Heure d'arrivée</Label>
                                <Input
                                    type="datetime-local"
                                    value={createForm.heureArrivee}
                                    onChange={(e) => setCreateField("heureArrivee", e.target.value)}
                                    className={validationErrors.heureArrivee ? "border-red-500" : ""}
                                />
                                {validationErrors.heureArrivee && (
                                    <p className="text-sm text-red-600">{validationErrors.heureArrivee}</p>
                                )}
                            </div>
                        </div>

                        <div className="space-y-2">
                            <Label>Avion</Label>
                            {availableAvions.length === 0 ? (
                                <p className="text-sm text-muted-foreground py-2">
                                    Aucun avion disponible
                                </p>
                            ) : (
                                <>
                                    <Select
                                        value={createForm.avionId}
                                        onValueChange={(v) => setCreateField("avionId", v)}
                                    >
                                        <SelectTrigger className={validationErrors.avionId ? "border-red-500" : ""}>
                                            <SelectValue placeholder="Sélectionner un avion" />
                                        </SelectTrigger>
                                        <SelectContent>
                                            {availableAvions.map((a) => (
                                                <SelectItem key={a.id} value={a.id}>
                                                    {a.immatriculation} - {a.type} ({a.capacite} places)
                                                </SelectItem>
                                            ))}
                                        </SelectContent>
                                    </Select>
                                    {validationErrors.avionId && (
                                        <p className="text-sm text-red-600">{validationErrors.avionId}</p>
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
                        <Button onClick={createVol} disabled={availableAvions.length === 0}>
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
                        <DialogTitle>Modifier le vol {openEdit?.numeroVol}</DialogTitle>
                    </DialogHeader>
                    <div className="grid gap-4 py-2">
                        <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
                            <div className="space-y-2">
                                <Label>Numéro de vol</Label>
                                <Input
                                    value={editForm.numeroVol}
                                    onChange={(e) => setEditForm((f) => ({ ...f, numeroVol: e.target.value.toUpperCase() }))}
                                    className={validationErrors.numeroVol ? "border-red-500" : ""}
                                />
                                {validationErrors.numeroVol && (
                                    <p className="text-sm text-red-600">{validationErrors.numeroVol}</p>
                                )}
                            </div>
                            <div className="space-y-2">
                                <Label>Origine</Label>
                                <Input
                                    value={editForm.origine}
                                    onChange={(e) => setEditForm((f) => ({ ...f, origine: e.target.value.toUpperCase() }))}
                                    className={validationErrors.origine ? "border-red-500" : ""}
                                    maxLength={3}
                                />
                                {validationErrors.origine && (
                                    <p className="text-sm text-red-600">{validationErrors.origine}</p>
                                )}
                            </div>
                            <div className="space-y-2">
                                <Label>Destination</Label>
                                <Input
                                    value={editForm.destination}
                                    onChange={(e) => setEditForm((f) => ({ ...f, destination: e.target.value.toUpperCase() }))}
                                    className={validationErrors.destination ? "border-red-500" : ""}
                                    maxLength={3}
                                />
                                {validationErrors.destination && (
                                    <p className="text-sm text-red-600">{validationErrors.destination}</p>
                                )}
                            </div>
                        </div>

                        <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
                            <div className="space-y-2">
                                <Label>Heure de départ</Label>
                                <Input
                                    type="datetime-local"
                                    value={editForm.heureDepart}
                                    onChange={(e) => setEditForm((f) => ({ ...f, heureDepart: e.target.value }))}
                                    className={validationErrors.heureDepart ? "border-red-500" : ""}
                                />
                                {validationErrors.heureDepart && (
                                    <p className="text-sm text-red-600">{validationErrors.heureDepart}</p>
                                )}
                            </div>
                            <div className="space-y-2">
                                <Label>Heure d'arrivée</Label>
                                <Input
                                    type="datetime-local"
                                    value={editForm.heureArrivee}
                                    onChange={(e) => setEditForm((f) => ({ ...f, heureArrivee: e.target.value }))}
                                    className={validationErrors.heureArrivee ? "border-red-500" : ""}
                                />
                                {validationErrors.heureArrivee && (
                                    <p className="text-sm text-red-600">{validationErrors.heureArrivee}</p>
                                )}
                            </div>
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
                            <Button onClick={() => updateVol(openEdit.id)}>
                                Enregistrer
                            </Button>
                        )}
                    </DialogFooter>
                </DialogContent>
            </Dialog>

            {/* CHANGE STATE DIALOG */}
            <Dialog open={!!openEtat} onOpenChange={(o) => {
                if (!o) {
                    setOpenEtat(null);
                    setValidationErrors({});
                }
            }}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>Changer l'état du vol</DialogTitle>
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
                            <Select value={etatForm} onValueChange={setEtatForm}>
                                <SelectTrigger className={validationErrors.etat ? "border-red-500" : ""}>
                                    <SelectValue />
                                </SelectTrigger>
                                <SelectContent>
                                    {VOL_ETATS.map((e) => (
                                        <SelectItem key={e} value={e}>{e}</SelectItem>
                                    ))}
                                </SelectContent>
                            </Select>
                            {validationErrors.etat && (
                                <p className="text-sm text-red-600">{validationErrors.etat}</p>
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
                        <Button onClick={changeEtat}>Mettre à jour</Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>

            {/* ASSIGN AVION DIALOG */}
            <Dialog open={!!openAssign} onOpenChange={(o) => {
                if (!o) {
                    setOpenAssign(null);
                    setAssignForm("");
                }
            }}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>Assigner un avion</DialogTitle>
                        <DialogDescription>Sélectionnez un avion disponible</DialogDescription>
                    </DialogHeader>
                    <div className="space-y-2 py-2">
                        <Label>Avion</Label>
                        {availableAvions.length === 0 ? (
                            <p className="text-sm text-muted-foreground py-2">
                                Aucun avion disponible
                            </p>
                        ) : (
                            <Select value={assignForm} onValueChange={setAssignForm}>
                                <SelectTrigger>
                                    <SelectValue placeholder="Sélectionner un avion" />
                                </SelectTrigger>
                                <SelectContent>
                                    {availableAvions.map((a) => (
                                        <SelectItem key={a.id} value={a.id}>
                                            {a.immatriculation} - {a.type} ({a.capacite} places)
                                        </SelectItem>
                                    ))}
                                </SelectContent>
                            </Select>
                        )}
                    </div>
                    <DialogFooter>
                        <Button variant="outline" onClick={() => {
                            setOpenAssign(null);
                            setAssignForm("");
                        }}>
                            Annuler
                        </Button>
                        <Button onClick={assignAvion} disabled={!assignForm}>
                            Assigner
                        </Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>

            {/* HISTORY DIALOG */}
            <Dialog open={!!openHistory} onOpenChange={(o) => !o && setOpenHistory(null)}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>Historique du vol</DialogTitle>
                        <DialogDescription>
                            Liste des changements d'état
                        </DialogDescription>
                    </DialogHeader>

                    <div className="max-h-80 overflow-y-auto py-4">
                        {historyList.length === 0 ? (
                            <p className="text-sm text-muted-foreground">Aucun historique</p>
                        ) : (
                            <div className="relative pl-4">
                                <div className="absolute left-2 top-0 bottom-0 w-px bg-border" />
                                <div className="space-y-6">
                                    {historyList
                                        .sort(
                                            (a, b) => new Date(a.changedAt).getTime() -
                                                new Date(b.changedAt).getTime()
                                        )
                                        .map((h, i) => (
                                            <div key={i} className="relative pl-6">
                                                <div
                                                    className={`absolute left-[-6px] top-[4px] h-3 w-3 rounded-full border-2 border-white shadow ${colorForEtat(
                                                        h.etat
                                                    )}`}
                                                />
                                                <div className="flex flex-col">
                                                    <span className="font-medium">{h.etat}</span>
                                                    <span className="text-xs text-muted-foreground">
                                                        {fmt.format(new Date(h.changedAt))}
                                                    </span>
                                                </div>
                                            </div>
                                        ))}
                                </div>
                            </div>
                        )}
                    </div>

                    <DialogFooter>
                        <Button variant="outline" onClick={() => setOpenHistory(null)}>
                            Fermer
                        </Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>
        </div>
    );
}
